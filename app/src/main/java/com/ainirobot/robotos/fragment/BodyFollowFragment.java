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
import com.ainirobot.coreservice.client.listener.ActionListener;
import com.ainirobot.coreservice.client.listener.CommandListener;
import com.ainirobot.coreservice.client.listener.Person;
import com.ainirobot.coreservice.client.person.PersonApi;
import com.ainirobot.coreservice.client.person.PersonListener;
import com.ainirobot.coreservice.client.person.PersonUtils;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.maputils.GsonUtil;

import java.util.List;

/**
 * 人体跟随功能，老版本 7.9可以支持，新版本不再支持
 * 人体跟随功能不稳定，会有丢失风险
 */
@Deprecated
public class BodyFollowFragment extends BaseFragment {
    private static final String TAG = "BodyFollowFragment";
    private static final double DEFAULT_MAX_DISTANCE = 3;
    private static final double DEFAULT_MAX_FACE_ANGLE_X = 60;
    private boolean mIsNeedInCompleteFace = false;
    private double mMaxDistance = DEFAULT_MAX_DISTANCE;
    private double mMaxFaceAngleX = DEFAULT_MAX_FACE_ANGLE_X;
    private boolean mIsNeedBody;
    private boolean isBodyFollowing;

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_body_follow_layout, null, false);
        bindViews(root);
        return root;
    }

    private void bindViews(View root) {
        root.findViewById(R.id.start_body_follow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBodyFollowing = false;
                PersonApi.getInstance().registerPersonListener(mPersonListener);
            }
        });
        root.findViewById(R.id.stop_body_follow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonApi.getInstance().unregisterPersonListener(mPersonListener);
                RobotApi.getInstance().stopBodyFollowAction(0);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PersonApi.getInstance().unregisterPersonListener(mPersonListener);
        RobotApi.getInstance().stopBodyFollowAction(0);
    }

    private PersonListener mPersonListener = new PersonListener() {
        @Override
        public void personChanged() {
            List<Person> faceList;
            if (mIsNeedInCompleteFace) {
                faceList = PersonApi.getInstance().getAllFaceList();
            } else {
                faceList = PersonApi.getInstance().getCompleteFaceList();
            }
            Person bestPerson = PersonUtils.getBestFace(faceList, mMaxDistance,
                    mMaxFaceAngleX);
            if (bestPerson == null && mIsNeedBody) {
                bestPerson = PersonUtils.getBestBody(PersonApi.getInstance().getAllBodyList(),
                        mMaxDistance);
            }
            if (isBodyFollowing) {
                return;
            }
            if (bestPerson != null) {
                isBodyFollowing = true;
                Log.d(TAG, "personChanged: " + GsonUtil.toJson(bestPerson));
                RobotApi.getInstance().startBodyFollowAction(0, bestPerson.getId(), new ActionListener() {
                    @Override
                    public void onError(int errorCode, String errorString) {
                        Log.d(TAG, "startBodyFollowAction onError : " + errorCode + " " + errorString);
                        switch (errorCode) {
                            case Definition.ERROR_SET_TRACK_FAILED:
                                Log.e(TAG, "onError: Target track failed");
                                break;
                            case Definition.ERROR_TARGET_NOT_FOUND:
                                Log.e(TAG, "onError: Target not found");
                                break;
                            case Definition.ERROR_FOLLOW_TIME_OUT:
                                Log.e(TAG, "onError: Guest lost");
                                break;
                            default:
                                Log.e(TAG, "onError: " + errorString);
                                break;
                        }
                    }

                    @Override
                    public void onStatusUpdate(int status, String data) {
                        Log.d(TAG, "startBodyFollowAction onStatusUpdate : " + status + " " + data);
                        switch (status) {
                            case Definition.STATUS_TRACK_TARGET_SUCCEED:
                                Log.d(TAG, "onStatusUpdate: Track success");
                                break;
                            case Definition.STATUS_GUEST_NEAR:
                                Log.d(TAG, "onStatusUpdate: Guest near");
                                break;
                            case Definition.STATUS_NAVI_OBSTACLES_AVOID:
                                Log.d(TAG, "onStatusUpdate: Pause due to obstacles within 1 meter");
                                break;
                            case Definition.STATUS_NAVI_OBSTACLES_DISAPPEAR:
                                Log.d(TAG, "onStatusUpdate: Obstacle disappear");
                                break;
                            default:
                                Log.d(TAG, "onStatusUpdate: " + data);
                                break;
                        }
                    }

                    @Override
                    public void onResult(int status, String responseString) {
                        Log.d(TAG, "startBodyFollowAction onResult : " + status + " " + responseString);
                    }
                });
            }
        }
    };

    public static Fragment newInstance() {
        return new BodyFollowFragment();
    }
}
