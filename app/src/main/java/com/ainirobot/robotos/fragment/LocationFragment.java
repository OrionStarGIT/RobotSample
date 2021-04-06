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
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.ainirobot.coreservice.client.Definition;
import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.listener.CommandListener;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationFragment extends BaseFragment {

    private double mCurrentX;
    private double mCurrentY;
    private double mCurrentTheta;

    private Button mIs_location;
    private Button mGet_location;
    private Button mSet_location;
    private Button mIs_in_location;
    private Button mRemove_location;
    private Button mSet_reception_point;

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_location_layout, null, false);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        mIs_location = (Button) root.findViewById(R.id.is_location);
        mGet_location = (Button) root.findViewById(R.id.get_location);
        mSet_location = (Button) root.findViewById(R.id.set_location);
        mIs_in_location = (Button) root.findViewById(R.id.is_in_location);
        mRemove_location = (Button) root.findViewById(R.id.remove_location);
        mSet_reception_point = (Button) root.findViewById(R.id.set_reception_point);


        mIs_in_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRobotInlocation();
            }
        });

        mSet_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPostEstimate();
            }
        });

        mIs_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRobotEstimate();
            }
        });

        mSet_reception_point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation();
            }
        });

        mGet_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        mRemove_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLocation();
            }
        });
    }


    /**
     * 判断机器人是否在位置点
     */
    private void isRobotInlocation() {
        try {
            JSONObject params = new JSONObject();
            params.put(Definition.JSON_NAVI_TARGET_PLACE_NAME, "接待点");
            params.put(Definition.JSON_NAVI_COORDINATE_DEVIATION, 2.0);

            RobotApi.getInstance().isRobotInlocations(0,
                    params.toString(), new CommandListener() {
                        @Override
                        public void onResult(int result, String message) {
                            try {
                                JSONObject json = new JSONObject(message);
                                json.getBoolean(Definition.JSON_NAVI_IS_IN_LOCATION);
                                LogTools.info("isRobotInlocation result: " + result + " message: "+ message);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置机器人初始坐标点
     */
    private void setPostEstimate() {
        if(mCurrentX == 0 || mCurrentY == 0){
            LogTools.info("坐标为空,请先获取当前坐标");
            return;
        }
        try {
            JSONObject params = new JSONObject();
            params.put(Definition.JSON_NAVI_POSITION_X, mCurrentX);
            params.put(Definition.JSON_NAVI_POSITION_Y, mCurrentY);
            params.put(Definition.JSON_NAVI_POSITION_THETA, mCurrentTheta);

            RobotApi.getInstance().setPoseEstimate(0, params.toString(), new CommandListener() {
                @Override
                public void onResult(int result, String message) {
                    LogTools.info("setPostEstimate result: " + result + " message: "+ message);
                    if ("succeed".equals(message)) {
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前是否已定位
     */
    private void isRobotEstimate() {
        RobotApi.getInstance().isRobotEstimate(0, new CommandListener() {
            @Override
            public void onResult(int result, String message) {
                LogTools.info("isRobotEstimate result: " + result + " message: " + message);
                if (!"true".equals(message)) {
                } else {
                }
            }
        });
    }

    /**
     * 设置当前位置名称
     */
    private void setLocation(){
        RobotApi.getInstance().setLocation(0, "接待点", new CommandListener() {
            @Override
            public void onResult(int result, String message) {
                LogTools.info("setLocation result: " + result + " message: " + message);
                if ("succeed".equals(message)) {
                } else {
                }
            }
        });
    }

    /**
     * 获取当前坐标点
     */
    private void getLocation(){
        RobotApi.getInstance().getPosition(0, new CommandListener() {
            @Override
            public void onResult(int result, String message) {
                LogTools.info("getLocation result: " + result + " message: "+ message);
                try {
                    JSONObject json = new JSONObject(message);
                    mCurrentX = json.getDouble(Definition.JSON_NAVI_POSITION_X);
                    mCurrentY = json.getDouble(Definition.JSON_NAVI_POSITION_Y);
                    mCurrentTheta = json.getDouble(Definition.JSON_NAVI_POSITION_THETA);
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 删除位置点
     */
    private void removeLocation(){
        RobotApi.getInstance().removeLocation(0, "接待点", new CommandListener() {
            @Override
            public void onResult(int result, String message) {
                LogTools.info("removeLocation result: " + result + " message: "+ message);
                if ("succeed".equals(message)) {
                } else {
                }
            }
        });
    }

    public static Fragment newInstance() {
        return new LocationFragment();
    }
}
