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
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.listener.ActionListener;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;

public class ChargeFragment extends BaseFragment {

    private Button mStop_auto_charge;
    private Button mStart_auto_charge;
    private Button mStop_charge_leave;

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_charge_layout, null, false);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        mStop_auto_charge = (Button) root.findViewById(R.id.stop_auto_charge);
        mStart_auto_charge = (Button) root.findViewById(R.id.start_auto_charge);
        mStop_charge_leave = (Button) root.findViewById(R.id.stop_charge_leave);

        mStart_auto_charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCharge();
            }
        });

        mStop_auto_charge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAutoChargeAction();
            }
        });

        mStop_charge_leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopChargingByApp();
            }
        });
    }

    /**
     * 开始自动回充
     */
    private void startCharge() {
        LogTools.info("开始自动回充");
        RobotApi.getInstance().startNaviToAutoChargeAction(0, 3 * Definition.MINUTE, mActionListener);
    }

    /**
     * 结束自动回充
     */
    private void stopAutoChargeAction() {
        LogTools.info("结束自动回充");
        RobotApi.getInstance().stopAutoChargeAction(0, true);
    }

    /**
     * 停止充电并脱离充电桩
     */
    private void stopChargingByApp() {
        LogTools.info("停止充电并脱离充电桩");
        RobotApi.getInstance().stopChargingByApp();
    }

    private ActionListener mActionListener = new ActionListener() {

        @Override
        public void onResult(int status, String responseString) throws RemoteException {
            switch (status) {
                case Definition.RESULT_OK:
                    LogTools.info("onResult result: " + status +"(充电成功)"+ " message: "+  responseString);
                    break;

                case Definition.RESULT_FAILURE:
                    LogTools.info("onResult result: " + status +"(充电失败)"+ " message: "+  responseString);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStatusUpdate(int status, String data) throws RemoteException {
            switch (status) {
                case Definition.STATUS_NAVI_GLOBAL_PATH_FAILED:
                    LogTools.info("onStatusUpdate result: " + status +"(全局路径规划失败)"+ " message: "+  data);
                    break;

                case Definition.STATUS_NAVI_OUT_MAP:
                    LogTools.info("onStatusUpdate result: " + status +"(目标点不能到达，引领目的地在地图外，有可能为地图与位置点不匹配，请重新设置位置点)"+ " message: "+  data);
                    break;

                case Definition.STATUS_NAVI_AVOID:
                    LogTools.info("onStatusUpdate result: " + status +"(前往回充点路线已被障碍物堵死)"+ " message: "+  data);
                    break;

                case Definition.STATUS_NAVI_AVOID_END:
                    LogTools.info("onStatusUpdate result: " + status +"(障碍物已移除)"+ " message: "+  data);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onError(int errorCode, String errorString, String extraData) throws RemoteException {
            super.onError(errorCode, errorString, extraData);
            LogTools.info("onError result: " + errorCode +"(充电回充失败)"+ " message: "+  errorString);
        }
    };

    public static Fragment newInstance() {
        return new ChargeFragment();
    }
}
