/*
 *  Copyright (C) 2017 OrionStar Technology Project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ainirobot.robotos.fragment;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ainirobot.coreservice.client.actionbean.Pose;
import com.ainirobot.coreservice.client.listener.Person;

import androidx.fragment.app.Fragment;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.actionbean.LeadingParams;
import com.ainirobot.coreservice.client.listener.ActionListener;
import com.ainirobot.coreservice.client.person.PersonApi;
import com.ainirobot.coreservice.client.person.PersonUtils;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;

import java.util.ArrayList;
import java.util.List;

public class LeadFragment extends BaseFragment {

    private Button mStop_lead_btn;
    private Button mStart_lead_btn;
    private EditText mLead_point;

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_lead_layout,null,false);
        bindViews(root);
        return root;
    }

    private void bindViews(View root) {
        mStop_lead_btn = (Button) root.findViewById(R.id.stop_lead_btn);
        mStart_lead_btn = (Button) root.findViewById(R.id.start_lead_btn);
        mLead_point = (EditText)root.findViewById(R.id.et_lead_point);

        mStart_lead_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCruise();
            }
        });

        mStop_lead_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLead();
            }
        });
    }

    private String getLeadPoint(){
        String leadPoint = mLead_point.getText().toString();
        if(TextUtils.isEmpty(leadPoint)){
            leadPoint = mLead_point.getHint().toString();
        }
        return leadPoint;
    }

    /*
    * 巡逻api测试
    */
    private void startCruise(){
        int reqId = 0;
        List<Pose> route = RobotApi.getInstance().getPlaceList();
        route.remove(0);
        route.remove(1);
        StringBuilder sb = new StringBuilder();
        for (Pose pose:route
             ) {
            sb.append(pose.getName());
            sb.append(',');
        }
        LogTools.info("Place list:"+sb.toString());
        int startPoint = 0;
        List<Integer> dockingPoints = new ArrayList<>();
        dockingPoints.add(1);
        RobotApi.getInstance().startCruise(reqId, route, startPoint, dockingPoints, cruiseListener);
    }

    /**
     * 开始引领
     */
    private void startLead() {
        LeadingParams params = new LeadingParams();

        //获取机器人视野内所有人体信息的人员列表

        //Get all bodies from robot
        List<Person> personList = PersonApi.getInstance().getAllBodyList();

        if(personList == null || personList.size() < 1){
            LogTools.info("No person found.  没有找到需要引领的人。");
            return;
        }
        //最佳身体意思是，机器人认为最像人身体的身体
        //best body mean it looks like body most
        Person person = PersonUtils.getBestBody(personList,3);
        LogTools.info("PersionID"+person.getId());
        params.setPersonId(person.getId());
        params.setDestinationName(getLeadPoint());
        params.setLostTimer(10 * 1000);
        params.setDetectDelay(5 * 1000);
        params.setMaxDistance(3);

        int reqId = 0;
        LogTools.info("startLead:" + getLeadPoint());
        RobotApi.getInstance().startLead(reqId, params, new ActionListener() {

            @Override
            public void onResult(int status, String responseString) throws RemoteException {
                switch (status) {
                    case Definition.RESULT_OK:
                        LogTools.info("onResult status :" + status + "(Lead success)" + " result:" + responseString);
                        LogTools.info("onResult status :" + status + "(成功将目标引领到目的地)" + " result:" + responseString);
                        break;

                    case Definition.ACTION_RESPONSE_STOP_SUCCESS:
                        LogTools.info("onResult status :" + status + "(Lead in progress, but stopped)" + " result:" + responseString);
                        LogTools.info("onResult status :" + status + "(成功将目标引领到目的地在引领执行中，主动调用stopLead，成功停止引领)" + " result:" + responseString);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onError(int errorCode, String errorString) throws RemoteException {
                switch (errorCode) {
                    case Definition.ERROR_NOT_ESTIMATE:
                        LogTools.info("onError errorCode :" + errorCode + "(not estimate)" + " result:" + errorString);
                        LogTools.info("onError errorCode :" + errorCode + "(当前未定位)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_SET_TRACK_FAILED:
                    case Definition.ERROR_TARGET_NOT_FOUND:
                        LogTools.info("onError errorCode :" + errorCode + "(No person found)" + " result:" + errorString);
                        LogTools.info("onError errorCode :" + errorCode + "(引领目标未找到)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_IN_DESTINATION:
                        LogTools.info("onError errorCode :" + errorCode + "(Already in destination)" + " result:" + errorString);
                        LogTools.info("onError errorCode :" + errorCode + "(当前已经在引领目的地)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_DESTINATION_CAN_NOT_ARRAIVE:
                        LogTools.info("onError errorCode :" + errorCode + "(Avoid timeout，default 20s run less than 0.1m)" + " result:" + errorString);
                        LogTools.info("onError errorCode :" + errorCode + "(避障超时，默认为机器人20s的前进距离不足0.1m)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_DESTINATION_NOT_EXIST:
                        LogTools.info("onError errorCode :" + errorCode + "(destination not exist)" + " result:" + errorString);
                        LogTools.info("onError errorCode :" + errorCode + "(引领目的地不存在)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_HEAD:
                        LogTools.info("onError errorCode :" + errorCode + "(Head can't work in the process of leading)" + " result:" + errorString);
                        LogTools.info("onError errorCode :" + errorCode + "(引领中操作头部云台失败)" + " result:" + errorString);
                        break;

                    case Definition.ACTION_RESPONSE_ALREADY_RUN:
                        LogTools.info("onError errorCode :" + errorCode + "(Leading function already started, please stop and then restart)" + " result:" + errorString);
                        LogTools.info("onError errorCode :" + errorCode + "(引领已经在进行中，请先停止上次引领，才能重新执行)" + " result:" + errorString);
                        break;

                    case Definition.ACTION_RESPONSE_REQUEST_RES_ERROR:
                        LogTools.info("onError errorCode :" + errorCode + "(Other function using wheels, please stop them first)" + " result:" + errorString);
                        LogTools.info("onError errorCode :" + errorCode + "(已经有需要控制底盘的接口调用，请先停止，才能继续调用)" + " result:" + errorString);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onStatusUpdate(int status, String data) throws RemoteException {
                switch (status) {
                    case Definition.STATUS_NAVI_OUT_MAP:
                        LogTools.info("onStatusUpdate status :" + status + "(can't find destination, maybe it's out of this map )" + " result:" + data);
                        LogTools.info("onStatusUpdate status :" + status + "(目标点不能到达，引领目的地在地图外，有可能为地图与位置点不匹配，请重新设置位置点)" + " result:" + data);
                        break;

                    case Definition.STATUS_NAVI_AVOID:
                        LogTools.info("onStatusUpdate status :" + status + "(can not avoid obstacles)" + " result:" + data);
                        LogTools.info("onStatusUpdate status :" + status + "(当前引领路线已被障碍物堵死)" + " result:" + data);
                        break;

                    case Definition.STATUS_NAVI_AVOID_END:
                        LogTools.info("onStatusUpdate status :" + status + "(obstacles removed)" + " result:" + data);
                        LogTools.info("onStatusUpdate status :" + status + "(障碍物已移除)" + " result:" + data);
                        break;

                    case Definition.STATUS_GUEST_FARAWAY:
                        LogTools.info("onStatusUpdate status :" + status + "(destination is too far away, please change maxDistance settings )" + " result:" + data);
                        LogTools.info("onStatusUpdate status :" + status + "(引领目标距离机器人太远，判断标准通过参数maxDistance设置)" + " result:" + data);
                        break;

                    case Definition.STATUS_DEST_NEAR:
                        LogTools.info("onStatusUpdate status :" + status + "(leading person in near destination)" + " result:" + data);
                        LogTools.info("onStatusUpdate status :" + status + "(引领目标进入机器人maxDistance范围内)" + " result:" + data);
                        break;

                    case Definition.STATUS_LEAD_NORMAL:
                        LogTools.info("onStatusUpdate status :" + status + "(leading started)" + " result:" + data);
                        LogTools.info("onStatusUpdate status :" + status + "(正式开始导航)" + " result:" + data);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * leading stop
     * 结束引领
     * isResetHW： 引领时会切换摄像头到后置摄像头，isResetHW是用于设置停止引领时是否恢复摄像头状态，
     * true: reset front camera, false: doesn't do anything
     * true：恢复摄像头为前置，false : 保持停止时的状态
     */
    private void stopLead() {
        RobotApi.getInstance().stopLead(0, true);
    }

    public static Fragment newInstance() {
        return new LeadFragment();
    }

    ActionListener cruiseListener = new ActionListener() {

        @Override
        public void onResult(int status, String responseString) throws RemoteException {
            LogTools.info("startCruise onResult : " + status + " || " + responseString);
            switch (status) {
                case Definition.RESULT_OK:
                    //巡航完成
                    break;

                case Definition.ACTION_RESPONSE_STOP_SUCCESS:
                    //在巡航过程中，主动调用stopCruise，成功停止巡航
                    break;
            }
        }

        @Override
        public void onStatusUpdate(int status, String data) throws RemoteException {
            LogTools.info("startCruise onStatusUpdate : " + status + " || " + data);
            switch (status) {
                case Definition.STATUS_NAVI_OUT_MAP:
                    //目标点不能到达，巡航点在地图外，请重新设置巡航路线
                    break;

                case Definition.STATUS_START_CRUISE:
                    //开始巡航
                    break;

                case Definition.STATUS_CRUISE_REACH_POINT:
                    int index = Integer.parseInt(data);
                    //巡航已到达第几个巡航点，index为巡航点在路线集合中的下标
                    break;

                case Definition.STATUS_NAVI_AVOID:
                    //当前巡航路线已被障碍物堵死
                    break;

                case Definition.STATUS_NAVI_AVOID_END:
                    //障碍物移除
                    break;
            }
        }

        @Override
        public void onError(int errorCode, String errorString) throws RemoteException {
            LogTools.info("startCruise onError : " + errorCode + " || " + errorString);
            switch (errorCode) {
                case Definition.ACTION_RESPONSE_ALREADY_RUN:
                    //巡航已经在进行
                    break;

                case Definition.ERROR_NOT_ESTIMATE:
                    //当前未定位
                    break;

                case Definition.ERROR_NAVIGATION_FAILED:
                    //巡航点导航失败
                    break;

                case Definition.ACTION_RESPONSE_REQUEST_RES_ERROR:
                    //已经有需要控制底盘的接口调用(例如：引领、导航等)，请先停止，才能继续调用
                    break;
            }
        }
    };
}
