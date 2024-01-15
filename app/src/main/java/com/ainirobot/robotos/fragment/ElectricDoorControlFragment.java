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
import com.ainirobot.coreservice.client.listener.CommandListener;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.maputils.GsonUtil;

public class ElectricDoorControlFragment extends BaseFragment {
    private static final String TAG = "ElectricDoorControl";

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_electric_door_control_layout, null, false);
        bindViews(root);
        return root;
    }

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
                    CanElectricDoorBean doorBean = GsonUtil.fromJson(message, CanElectricDoorBean.class);
                    //两种方式可以判断门的开关状态，door1和 door2是上面的两扇仓门，door3和door4是下面的两扇仓门
                    Log.d(TAG, "door1 status: " + (doorBean.getDoor1() == Definition.CAN_DOOR_STATUS_OPEN ? "open" : "close"));
                    Log.d(TAG, "door2 status: " + (doorBean.getDoor2() == Definition.CAN_DOOR_STATUS_OPEN ? "open" : "close"));
                    Log.d(TAG, "door3 status: " + (doorBean.getDoor3() == Definition.CAN_DOOR_STATUS_OPEN ? "open" : "close"));
                    Log.d(TAG, "door4 status: " + (doorBean.getDoor4() == Definition.CAN_DOOR_STATUS_OPEN ? "open" : "close"));
//                    Log.d(TAG, "door1 status: " + (doorBean.getDoor2() == Definition.CAN_DOOR_STATUS_CLOSE ? "close" : "open"));
//                    Log.d(TAG, "door2 status: " + (doorBean.getDoor2() == Definition.CAN_DOOR_STATUS_CLOSE ? "close" : "open"));
//                    Log.d(TAG, "door3 status: " + (doorBean.getDoor3() == Definition.CAN_DOOR_STATUS_CLOSE ? "close" : "open"));
//                    Log.d(TAG, "door4 status: " + (doorBean.getDoor4() == Definition.CAN_DOOR_STATUS_CLOSE ? "close" : "open"));

                    //门的夹手状态
                    Log.d(TAG, "doorUp status is " + (doorBean.getUpStatus() == Definition.CAN_DOOR_STATUS_BLOCK_AND_BOUNCE ? "block and bounce" : "normal"));
                    Log.d(TAG, "doorDown status is " + (doorBean.getDownStatus() == Definition.CAN_DOOR_STATUS_BLOCK_AND_BOUNCE ? "block and bounce" : "normal"));
                } else {
                    Log.d(TAG, "getElectricDoorStatus onResult: Failure to get status");
                }
            }
        });
    }

    public static Fragment newInstance() {
        return new ElectricDoorControlFragment();
    }
}
