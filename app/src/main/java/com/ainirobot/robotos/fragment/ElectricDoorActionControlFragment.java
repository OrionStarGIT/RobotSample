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
import android.util.Log;
import android.view.View;
import androidx.fragment.app.Fragment;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.listener.ActionListener;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;

public class ElectricDoorActionControlFragment extends BaseFragment {
    private static final String TAG = "ElecDoorActionControl";

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_electric_door_action_control_layout, null, false);
        bindViews(root);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void bindViews(View root) {
        Log.d(TAG, "bindViews: ");
        initCmdView(root, R.id.open_first_door, Definition.CAN_DOOR_DOOR1_DOOR2_OPEN);
        initCmdView(root, R.id.close_first_door, Definition.CAN_DOOR_DOOR1_DOOR2_CLOSE);

        initCmdView(root, R.id.open_second_door, Definition.CAN_DOOR_DOOR3_DOOR4_OPEN);
        initCmdView(root, R.id.close_second_door, Definition.CAN_DOOR_DOOR3_DOOR4_CLOSE);

        initCmdView(root, R.id.open_all_door, Definition.CAN_DOOR_ALL_OPEN);
        initCmdView(root, R.id.close_all_door, Definition.CAN_DOOR_ALL_CLOSE);
    }

    private void initCmdView(View root, int viewId, final int doorCmd) {
        root.findViewById(viewId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                RobotApi.getInstance().startControlElectricDoor(0, doorCmd, new ActionListener() {
                    @Override
                    public void onResult(int result, String message, String extraData) {
                        Log.d(TAG, "startControlElectricDoor result:" + result + " message:" + message);
                        LogTools.info("startControlElectricDoor result:" + result + " message:" + message);
                        if (result == Definition.RESULT_OK) {
                            Log.d(TAG, "startControlElectricDoor success");
                            LogTools.info("startControlElectricDoor success");
                        } else {
                            Log.d(TAG, "startControlElectricDoor failed");
                            LogTools.info("startControlElectricDoor failed");
                        }
                    }

                    @Override
                    public void onError(int errorCode, String errorString, String extraData) throws RemoteException {
                        Log.d(TAG, "startControlElectricDoor onError: errorCode:" + errorCode + " errorString:" + errorString);
                        LogTools.info("startControlElectricDoor onError: errorCode:" + errorCode + " errorString:" + errorString);
                        if (errorCode == Definition.ERROR_ELECTRIC_DOOR_BLOCK) {
                            Log.d(TAG, "startControlElectricDoor onError: electric door block");
                            LogTools.info("startControlElectricDoor onError: electric door block");
                        } else if (errorCode == Definition.ERROR_ELECTRIC_DOOR_UPPER_BLOCK) {
                            Log.d(TAG, "startControlElectricDoor onError: electric door upper block");
                            LogTools.info("startControlElectricDoor onError: electric door upper block");
                        } else if (errorCode == Definition.ERROR_ELECTRIC_DOOR_LOWER_BLOCK) {
                            Log.d(TAG, "startControlElectricDoor onError: electric door lower block");
                            LogTools.info("startControlElectricDoor onError: electric door lower block");
                        } else if (errorCode == Definition.ERROR_ELECTRIC_DOOR_TIMEOUT) {
                            Log.d(TAG, "startControlElectricDoor onError: electric door timeout");
                            LogTools.info("startControlElectricDoor onError: electric door timeout");
                        }
                    }

                    @Override
                    public void onStatusUpdate(int status, String data, String extraData) throws RemoteException {
                        Log.d(TAG, "onStatusUpdate: status:" + status + " data:" + data);
                        LogTools.info("onStatusUpdate: status:" + status + " data:" + data);
                        if (status == Definition.STATUS_ELECTRIC_DOOR_BLOCK) {
                            Log.d(TAG, "onStatusUpdate: electric door block");
                            LogTools.info("onStatusUpdate: electric door block");
                        } else if (status == Definition.STATUS_ELECTRIC_DOOR_UPPER_BLOCK) {
                            Log.d(TAG, "onStatusUpdate: electric door upper block");
                            LogTools.info("onStatusUpdate: electric door upper block");
                        } else if (status == Definition.STATUS_ELECTRIC_DOOR_LOWER_BLOCK) {
                            Log.d(TAG, "onStatusUpdate: electric door lower block");
                            LogTools.info("onStatusUpdate: electric door lower block");
                        }
                    }
                });
            }
        });
    }

    public static Fragment newInstance() {
        return new ElectricDoorActionControlFragment();
    }
}
