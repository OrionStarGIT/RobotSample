package com.ainirobot.robotos.fragment;

import android.Manifest;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.StatusListener;
import com.ainirobot.coreservice.client.actionbean.PlaceBean;
import com.ainirobot.coreservice.client.ashmem.ShareMemoryApi;
import com.ainirobot.coreservice.client.listener.ActionListener;
import com.ainirobot.coreservice.client.listener.CommandListener;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.MainActivity;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.maputils.Constant;
import com.ainirobot.robotos.maputils.DialogConfirm;
import com.ainirobot.robotos.maputils.DialogUtils;
import com.ainirobot.robotos.maputils.GlobalData;
import com.ainirobot.robotos.maputils.GsonUtil;
import com.ainirobot.robotos.view.BackView;
import com.ainirobot.robotos.view.MapView;
import com.ainirobot.robotos.maputils.MapppUtils;
import com.ainirobot.robotos.maputils.Pose2d;
import com.ainirobot.robotos.maputils.PoseBean;
import com.ainirobot.robotos.maputils.RoverMap;
import com.ainirobot.robotos.maputils.SpecialPlaceUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class NavFragment extends Fragment {

    private MapView mMapView;
    private final static String MAP_DIR = "/robot/map";
    private final static String ROBOT_MAP_DIR = Environment.getExternalStorageDirectory() + MAP_DIR;
    private final static String MAP_PGM = "pgm.zip";
    public static boolean isCreatingMap = false;
    private static final String TAG = "NavFragment";
    private BackView mBackView;
    private boolean mIsEstimate;
    private RoverMap mRoverMap;
    private String placeName1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_nav, null, false);
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        initView(root);
        return root;
    }

    private void initView(View root) {
        mMapView = root.findViewById(R.id.map_view);
        mBackView = root.findViewById(R.id.edit_back);
        /*
         * 获得当前地图名
         * Get the current map name
         * */
        RobotApi.getInstance().getMapName(0, new CommandListener() {
            @Override
            public void onResult(int result, String message, String extraData) {
                super.onResult(result, message, extraData);
                if (!TextUtils.isEmpty(message)) {
                    //message为地图名称
                    getMap(message);
                }
            }
        });

        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopNavigation();
                MainActivity.getInstance().switchFragment(MainFragment.newInstance());
            }
        });

        mMapView.setMode(MapView.MapMode.POINT);
        /*
         * 注册MapView地点点击监听
         * registOnPlaceClickListener
         * */
        mMapView.registOnPlaceClickListener(mOnPlaceClickListener);
    }


    /*
     * getMap
     * 获得当前地图
     * 地图具有当前机器人位置信息，方向坐标与可点击导航的位置点位
     * Get the current map
     * The map has current robot position information, direction coordinates and clickable navigation positions
     * */
    private void getMap(final String name) {
        Log.d(TAG, "getMapPgmPFD: mapName=" + name);
        //获取 map.pgm 文件描述符
        ParcelFileDescriptor mapPgmPFD = ShareMemoryApi.getInstance().getMapPgmPFD(name);
        FileDescriptor fd = mapPgmPFD.getFileDescriptor();
        FileInputStream fileInputStream = new FileInputStream(fd);
        //从文件描述符读取数据流，解析为 RoverMap（此逻辑和之前一致）
        mRoverMap = MapppUtils.loadPFD2RoverMap(fileInputStream);

        if (mRoverMap != null) {
            mMapView.setBitmap(mRoverMap.bitmap);
            Log.d(TAG, "mRoverMap.res: " + mRoverMap.res);
            mMapView.setResolution(mRoverMap.res);
        } else {
            String mapPath = ROBOT_MAP_DIR + File.separator + name + File.separator + MAP_PGM;
            mRoverMap = MapppUtils.loadMap(mapPath);
            if (mRoverMap != null) {
                mMapView.setBitmap(mRoverMap.bitmap);
                Log.d(TAG, "mRoverMap.res: " + mRoverMap.res);
                mMapView.setResolution(mRoverMap.res);
            } else {
                Log.d(TAG, "parse map fail");
            }
        }
        GlobalData.getInstance().setEditMapData(mMapView, mRoverMap);

        /*
         * getInternationalPlaceList
         * 获取地图位置点
         * Get map location point
         * */
        RobotApi.getInstance().getInternationalPlaceList(0, name, new CommandListener() {
            @Override
            public void onResult(int result, String message, String extraData) {
                super.onResult(result, message, extraData);

                Log.i(TAG, "getPlaceList onResult, " + message);

                if (result == Definition.RESULT_OK &&
                        !TextUtils.isEmpty(message) && !"timeout".equals(message)) {
                    List<PoseBean> poseBeans = new ArrayList<>();
                    try {
                        Gson gson = new Gson();
                        List<PlaceBean> placeBeanList = gson.fromJson(message, new TypeToken<List<PlaceBean>>() {
                        }.getType());
                        for (PlaceBean placeBean : placeBeanList) {
                            if (SpecialPlaceUtil.isChargingPoint(placeBean) ||
                                    (RobotApi.getInstance().isChargePileExits() &&
                                            SpecialPlaceUtil.isNavigatorPoint(
                                                    Constant.NavigatorPoint.POINT2,
                                                    placeBean))) {
                                continue;
                            }
                            String placename = placeBean.getPlaceName();
                            if (null != placename) {
                                poseBeans.add(new PoseBean(placename,
                                        MapppUtils.pose2PixelByRoverMap(mRoverMap
                                                , new Pose2d(placeBean.getPointX(),
                                                        placeBean.getPointY(),
                                                        placeBean.getPointTheta(),
                                                        placeBean.getPlaceStatus()))));
                            }

                            /*
                             * setOrigin
                             * 设置机器人当前位置
                             * Set the current position of the robot
                             * */
                            mMapView.setOrigin(MapppUtils.pose2PixelByRoverMap(mRoverMap, new Pose2d(placeBean.getPointX(),
                                    placeBean.getPointY(),
                                    placeBean.getPointTheta(),
                                    placeBean.getPlaceStatus())));
                        }
                        mMapView.setPoseBeans(poseBeans);
                        GlobalData.getInstance().setPoseBeanList(poseBeans, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /*
     * OnPlaceClickListener
     * 地图位置点点击监听
     * Click on the map location to monitor
     * */
    MapView.OnPlaceClickListener mOnPlaceClickListener = new MapView.OnPlaceClickListener() {
        @Override
        public void onPlaceClick(final String placeName) {
            DialogUtils.showGoNavigation(getContext(), placeName,
                    new DialogConfirm.ConfirmCallBack() {
                        @Override
                        public void confirmClick() {
                            placeName1 = placeName;
                            startNavigation(placeName);
                        }
                    }, new DialogConfirm.CancelBtnCallBack() {
                        @Override
                        public void cancelClick() {
                            Toast.makeText(getContext(), "点击成功", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    };

    /**
     * startNavigation
     * 导航到指定位置
     */
    public static void startNavigation(String placeName) {

        RobotApi.getInstance().startNavigation(0, placeName, 1.5, 10 * 1000, mNavigationListener);
    }

    /**
     * stopNavigation
     * 停止导航到指定位置
     */
    private void stopNavigation() {
        RobotApi.getInstance().stopNavigation(0);
    }

    private boolean isCurrentMap() {
        return TextUtils.equals(placeName1,
                GlobalData.getInstance().getCurrentMapName());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isCurrentMap()) {
            RobotApi.getInstance().registerStatusListener(
                    Constant.CoreDef.POSE_LISTEN, mStatusPoseListener);

            mIsEstimate = RobotApi.getInstance().isRobotEstimate();
            RobotApi.getInstance().registerStatusListener(
                    Definition.STATUS_POSE_ESTIMATE, mEstimateStateListen);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        RobotApi.getInstance().unregisterStatusListener(mStatusPoseListener);
        RobotApi.getInstance().unregisterStatusListener(mEstimateStateListen);
        //释放 service 层资源
        ShareMemoryApi.getInstance().releaseGetMapPgmPFD();
    }

    private StatusListener mStatusPoseListener = new StatusListener() {
        long preTime = System.currentTimeMillis();

        @Override
        public void onStatusUpdate(String type, String value) {
            Pose2d pose2d = GsonUtil.fromJson(value, Pose2d.class);
            long curTime = System.currentTimeMillis();
            if (curTime - preTime > 2500) {
                preTime = curTime;
                Log.d(TAG, "onStatusUpdate. " + pose2d);
            }
            GlobalData.getInstance().setNewestLocation(pose2d);
            onMapPose2d(pose2d);
        }
    };

    private void onMapPose2d(final Pose2d pose2d) {
        if (null != mRoverMap) {
            if (mIsEstimate) {
                mMapView.setOrigin(MapppUtils.pose2PixelByRoverMap(mRoverMap, pose2d));
//                mMapView.setResolution(mRoverMap.res);
            }
        }
    }

    private StatusListener mEstimateStateListen = new StatusListener() {
        @Override
        public void onStatusUpdate(String type, final String data) {
            Log.d(TAG, "onStatusUpdate type = " + type + ", data = " + data);
            try {
                JSONObject jsonObject = new JSONObject(data);
                mIsEstimate = jsonObject.optBoolean("isPoseEstimate", false);
            } catch (Exception e) {
                mIsEstimate = false;
            }
            if (!mIsEstimate) {
                mMapView.setOrigin(null);
            }
        }
    };

    private static ActionListener mNavigationListener = new ActionListener() {

        @Override
        public void onResult(int status, String response) throws RemoteException {

            switch (status) {
                case Definition.RESULT_OK:
                    if ("true".equals(response)) {
                        LogTools.info("startNavigation result: " + status + "(Navigation success)" + " message: " + response);
                        LogTools.info("startNavigation result: " + status + "(导航成功)" + " message: " + response);
                    } else {
                        LogTools.info("startNavigation result: " + status + "(Navigation failed)" + " message: " + response);
                        LogTools.info("startNavigation result: " + status + "(导航失败)" + " message: " + response);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int errorCode, String errorString) throws RemoteException {
            switch (errorCode) {
                case Definition.ERROR_NOT_ESTIMATE:
                    LogTools.info("onError result: " + errorCode + "(not estimate)" + " message: " + errorString);
                    LogTools.info("onError result: " + errorCode + "(当前未定位)" + " message: " + errorString);
                    break;
                case Definition.ERROR_IN_DESTINATION:
                    LogTools.info("onError result: " + errorCode + "(in destination, no action)" + " message: " + errorString);
                    LogTools.info("onError result: " + errorCode + "(当前机器人已经在目的地范围内)" + " message: " + errorString);
                    break;
                case Definition.ERROR_DESTINATION_NOT_EXIST:
                    LogTools.info("onError result: " + errorCode + "(destination not exist)" + " message: " + errorString);
                    LogTools.info("onError result: " + errorCode + "(导航目的地不存在)" + " message: " + errorString);
                    break;
                case Definition.ERROR_DESTINATION_CAN_NOT_ARRAIVE:
                    LogTools.info("onError result: " + errorCode + "(avoid timeout, can not arrive)" + " message: " + errorString);
                    LogTools.info("onError result: " + errorCode + "(避障超时，目的地不能到达，超时时间通过参数设置)" + " message: " + errorString);
                    break;
                case Definition.ACTION_RESPONSE_ALREADY_RUN:
                    LogTools.info("onError result: " + errorCode + "(already started, please stop first)" + " message: " + errorString);
                    LogTools.info("onError result: " + errorCode + "(当前接口已经调用，请先停止，才能再次调用)" + " message: " + errorString);
                    break;
                case Definition.ACTION_RESPONSE_REQUEST_RES_ERROR:
                    LogTools.info("onError result: " + errorCode + "(wheels are busy for other actions, please stop first)" + " message: " + errorString);
                    LogTools.info("onError result: " + errorCode + "(已经有需要控制底盘的接口调用，请先停止，才能继续调用)" + " message: " + errorString);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStatusUpdate(int status, String data) throws RemoteException {
            switch (status) {
                case Definition.STATUS_NAVI_AVOID:
                    LogTools.info("onStatusUpdate result: " + status + "(can not avoid obstacles)" + " message: " + data);
                    LogTools.info("onStatusUpdate result: " + status + "(当前路线已经被障碍物堵死)" + " message: " + data);
                    break;
                case Definition.STATUS_NAVI_AVOID_END:
                    LogTools.info("onStatusUpdate result: " + status + "(Obstacle removed)" + " message: " + data);
                    LogTools.info("onStatusUpdate result: " + status + "(障碍物已移除)" + " message: " + data);
                    break;
                default:
                    break;
            }
        }
    };

    public static Fragment newInstance() {
        return new NavFragment();
    }
}