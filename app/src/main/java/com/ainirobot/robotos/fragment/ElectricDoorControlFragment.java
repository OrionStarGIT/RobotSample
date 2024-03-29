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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.ainirobot.coreservice.bean.CanElectricDoorBean;
import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.StatusListener;
import com.ainirobot.coreservice.client.listener.CommandListener;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.maputils.GsonUtil;

public class ElectricDoorControlFragment extends BaseFragment {
    private static final String TAG = "ElectricDoorControl";

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_electric_door_control_layout, null, false);
        bindViews(root);
        //注册电动门状态监听，获取电动门状态，根据自己业务在需要地方注册
        //当电动门状态发生变化时，会回调onStatusUpdate方法，例如调用开门、关门指令后，电动门状态会发生变化
        //如果需要在任意时机获取电动门状态，可以调用getElectricDoorStatus方法
        RobotApi.getInstance().registerStatusListener(Definition.STATUS_CAN_ELECTRIC_DOOR_CTRL, statusListener);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //取消注册电动门状态监听
        RobotApi.getInstance().unregisterStatusListener(statusListener);
    }

    private StatusListener statusListener = new StatusListener() {
        @Override
        public void onStatusUpdate(String type, String data) {
            if (TextUtils.equals(type, Definition.STATUS_CAN_ELECTRIC_DOOR_CTRL)) {
                Log.d(TAG, "electric door status update data:" + data);
                handlerElectricResult(data);
            }
        }
    };

    private void bindViews(View root) {
        initCmdView(root, R.id.open_first_door, Definition.CAN_DOOR_DOOR1_DOOR2_OPEN);
        initCmdView(root, R.id.close_first_door, Definition.CAN_DOOR_DOOR1_DOOR2_CLOSE);

        initCmdView(root, R.id.open_second_door, Definition.CAN_DOOR_DOOR3_DOOR4_OPEN);
        initCmdView(root, R.id.close_second_door, Definition.CAN_DOOR_DOOR3_DOOR4_CLOSE);

        initCmdView(root, R.id.open_all_door, Definition.CAN_DOOR_ALL_OPEN);
        initCmdView(root, R.id.close_all_door, Definition.CAN_DOOR_ALL_CLOSE);

        root.findViewById(R.id.get_door_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getElectricDoorStatus();
            }
        });
    }

    private void initCmdView(View root, int viewId, final int doorCmd) {
        root.findViewById(viewId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 电动门控制
                 * 业务调用前，需要先获取电动门状态，判断门是否在运动中，如果在运动中，不要执行以下开关门指令
                 */
                RobotApi.getInstance().setElectricDoorCtrl(0, doorCmd, new CommandListener() {
                    @Override
                    public void onResult(int result, String message, String extraData) {
                        super.onResult(result, message, extraData);
                        Log.d(TAG, "setElectricDoorCtrl result:" + result + " message:" + message);
                        if (result == 1 && TextUtils.equals(message, Definition.SUCCEED)) {
                            Log.d(TAG, "onResult: The electric door is controlled successfully");
                        } else {
                            Log.d(TAG, "onResult: The electric door is controlled failed");
                        }
                    }
                });
            }
        });
    }

    private void getElectricDoorStatus() {
        RobotApi.getInstance().getElectricDoorStatus(0, new CommandListener() {
            @Override
            public void onResult(int result, String message, String extraData) {
                super.onResult(result, message, extraData);
                Log.d(TAG, "getElectricDoorStatus result:" + result + " message:" + message);
                if (result == 1 && !TextUtils.isEmpty(message)) {
                    handlerElectricResult(message);
                } else {
                    Log.d(TAG, "getElectricDoorStatus onResult: Failure to get status");
                }
            }
        });
    }

    private void handlerElectricResult(String message) {
        CanElectricDoorBean doorBean = GsonUtil.fromJson(message, CanElectricDoorBean.class);
        //door1和 door2是上面的两扇仓门，door3和door4是下面的两扇仓门
        //上仓门的状态
        if (doorBean.getDoor1() == Definition.CAN_DOOR_STATUS_RUNNING || doorBean.getDoor2() == Definition.CAN_DOOR_STATUS_RUNNING) {
            Log.d(TAG, "upper door is running");
            //当仓门正在运动时，不要执行开关门指令
        }
        if (doorBean.getDoor1() == Definition.CAN_DOOR_STATUS_OPEN && doorBean.getDoor2() == Definition.CAN_DOOR_STATUS_OPEN) {
            Log.d(TAG, "upper door is open");
        }
        if (doorBean.getDoor1() == Definition.CAN_DOOR_STATUS_CLOSE && doorBean.getDoor2() == Definition.CAN_DOOR_STATUS_CLOSE) {
            Log.d(TAG, "upper door is close");
        }
        //下仓门的状态
        if (doorBean.getDoor3() == Definition.CAN_DOOR_STATUS_RUNNING || doorBean.getDoor4() == Definition.CAN_DOOR_STATUS_RUNNING) {
            Log.d(TAG, "lower door is running");
            //当仓门正在运动时，不要执行开关门指令
        }
        if (doorBean.getDoor3() == Definition.CAN_DOOR_STATUS_OPEN && doorBean.getDoor4() == Definition.CAN_DOOR_STATUS_OPEN) {
            Log.d(TAG, "lower door is open");
        }
        if (doorBean.getDoor3() == Definition.CAN_DOOR_STATUS_CLOSE && doorBean.getDoor4() == Definition.CAN_DOOR_STATUS_CLOSE) {
            Log.d(TAG, "lower door is close");
        }

        //门的堵转状态，开门、关门时可能会出现堵转
        //关门时，被堵
        if (doorBean.getUpStatus() == Definition.CAN_DOOR_STATUS_BLOCK_AND_BOUNCE) {
            Log.d(TAG, "upper door is block and bounce");
        }
        if (doorBean.getDownStatus() == Definition.CAN_DOOR_STATUS_BLOCK_AND_BOUNCE) {
            Log.d(TAG, "lower door is block and bounce");
        }
        //开门时，被堵
        if (doorBean.getUpStatus() == Definition.CAN_DOOR_STATUS_BLOCKING_STOP) {
            Log.d(TAG, "upper door is blocking stop");
        }
        if (doorBean.getDownStatus() == Definition.CAN_DOOR_STATUS_BLOCKING_STOP) {
            Log.d(TAG, "lower door is blocking stop");
        }
    }

    public static Fragment newInstance() {
        return new ElectricDoorControlFragment();
    }
}
