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


import android.app.Dialog;
import android.os.Handler;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;


import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.coreservice.client.listener.CommandListener;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;


public class SportFragment extends BaseFragment {

    private Button mGo_back;
    private Button mTurn_left;
    private Button mStop_move;
    private Button mGo_forward;
    private Button mTurn_right;


    private Button mHead_up;
    private Button mHead_down;
    private Button mHead_left;
    private Button mHead_right;

    private static int reqId = 0;

    private Dialog movingDialog;
    private Handler autoStopHandler = new Handler();

    private void showMovingDialog() {
        movingDialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        movingDialog.setContentView(R.layout.moving_dialog_layout);
        movingDialog.setCancelable(false);
        movingDialog.show();

        TextView movingText = movingDialog.findViewById(R.id.moving_text);
        movingText.setText("moving...tap to stop");
        movingText.setTextSize(30); // 设置大字号，根据需要调整
        movingText.setTextColor(Color.WHITE); // 设置字体颜色为白色
        movingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK)); // 设置背景颜色为黑色

        // 应用触摸监听器到 Dialog 的 Window 对象
        movingDialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    stopMoving();
                    return true;
                }
                return false;
            }
        });

        // 设置3秒后自动停止
        autoStopHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopMoving();
            }
        }, 3000);
    }

    private void stopMoving() {
        if (movingDialog != null && movingDialog.isShowing()) {
            movingDialog.dismiss();
        }
        autoStopHandler.removeCallbacksAndMessages(null); // 取消所有的计时器回调
        RobotApi.getInstance().stopMove(0, mMotionListener);
    }


    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_sport_layout, null, false);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        mGo_back = (Button) root.findViewById(R.id.go_back);
        mGo_forward = (Button) root.findViewById(R.id.go_forward);
        mStop_move = (Button) root.findViewById(R.id.stop_move);
        mTurn_left = (Button) root.findViewById(R.id.turn_left);
        mTurn_right = (Button) root.findViewById(R.id.turn_right);
        mHead_up = (Button) root.findViewById(R.id.head_up);
        mHead_down = (Button) root.findViewById(R.id.head_down);
        mHead_left = (Button) root.findViewById(R.id.head_left);
        mHead_right = (Button) root.findViewById(R.id.head_right);

        mGo_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().goForward(0, 0.4f, mMotionListener);
                showMovingDialog(); // 显示全屏对话框
            }
        });

        mGo_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().goBackward(0, 0.3f, mMotionListener);
                showMovingDialog(); // 显示全屏对话框
            }
        });

        mTurn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().turnLeft(0, 25f, mMotionListener);
            }
        });

        mTurn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().turnRight(0, 25f, mMotionListener);
            }
        });

        mStop_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().stopMove(0, mMotionListener);
            }
        });

        mHead_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().moveHead(reqId++, "relative", "relative", 0, -10, mMotionListener);
            }
        });
        mHead_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().moveHead(reqId++, "relative", "relative", 0, 10, mMotionListener);
            }
        });
        mHead_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().moveHead(reqId++, "relative", "relative", -10, 0, mMotionListener);
            }
        });
        mHead_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().moveHead(reqId++, "relative", "relative", 10, 0, mMotionListener);
            }
        });
    }

    private CommandListener mMotionListener = new CommandListener() {
        @Override
        public void onResult(int result, String message) {
            LogTools.info("result: " + result + " message:" + message);
            if ("succeed".equals(message)) {
            } else {
            }
        }
    };

    public static Fragment newInstance() {
        return new SportFragment();
    }
}
