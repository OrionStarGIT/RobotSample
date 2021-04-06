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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.actionbean.LeadingParams;
import com.ainirobot.coreservice.client.listener.ActionListener;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;

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
                startLead();
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
    /**
     * 开始引领
     */
    private void startLead() {
        LeadingParams params = new LeadingParams();
        params.setPersonId(0);
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
                        LogTools.info("onResult status :" + status + "(成功将目标引领到目的地)" + " result:" + responseString);
                        break;

                    case Definition.ACTION_RESPONSE_STOP_SUCCESS:
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
                        LogTools.info("onError errorCode :" + errorCode + "(当前未定位)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_SET_TRACK_FAILED:
                    case Definition.ERROR_TARGET_NOT_FOUND:
                        LogTools.info("onError errorCode :" + errorCode + "(引领目标未找到)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_IN_DESTINATION:
                        LogTools.info("onError errorCode :" + errorCode + "(当前已经在引领目的地)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_DESTINATION_CAN_NOT_ARRAIVE:
                        LogTools.info("onError errorCode :" + errorCode + "(避障超时，默认为机器人20s的前进距离不足0.1m)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_DESTINATION_NOT_EXIST:
                        LogTools.info("onError errorCode :" + errorCode + "(引领目的地不存在)" + " result:" + errorString);
                        break;

                    case Definition.ERROR_HEAD:
                        LogTools.info("onError errorCode :" + errorCode + "(引领中操作头部云台失败)" + " result:" + errorString);
                        break;

                    case Definition.ACTION_RESPONSE_ALREADY_RUN:
                        LogTools.info("onError errorCode :" + errorCode + "(引领已经在进行中，请先停止上次引领，才能重新执行)" + " result:" + errorString);
                        break;

                    case Definition.ACTION_RESPONSE_REQUEST_RES_ERROR:
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
                        LogTools.info("onStatusUpdate status :" + status + "(目标点不能到达，引领目的地在地图外，有可能为地图与位置点不匹配，请重新设置位置点)" + " result:" + data);
                        break;

                    case Definition.STATUS_NAVI_AVOID:
                        LogTools.info("onStatusUpdate status :" + status + "(当前引领路线已被障碍物堵死)" + " result:" + data);
                        break;

                    case Definition.STATUS_NAVI_AVOID_END:
                        LogTools.info("onStatusUpdate status :" + status + "(障碍物已移除)" + " result:" + data);
                        break;

                    case Definition.STATUS_GUEST_FARAWAY:
                        LogTools.info("onStatusUpdate status :" + status + "(引领目标距离机器人太远，判断标准通过参数maxDistance设置)" + " result:" + data);
                        break;

                    case Definition.STATUS_DEST_NEAR:
                        LogTools.info("onStatusUpdate status :" + status + "(引领目标进入机器人maxDistance范围内)" + " result:" + data);
                        break;

                    case Definition.STATUS_LEAD_NORMAL:
                        LogTools.info("onStatusUpdate status :" + status + "(正式开始导航)" + " result:" + data);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * 结束引领
     * isResetHW： 引领时会切换摄像头到后置摄像头，isResetHW是用于设置停止引领时是否恢复摄像头状态，
     * true：恢复摄像头为前置，false : 保持停止时的状态
     */
    private void stopLead() {
        RobotApi.getInstance().stopLead(0, true);
    }

    public static Fragment newInstance() {
        return new LeadFragment();
    }

}
