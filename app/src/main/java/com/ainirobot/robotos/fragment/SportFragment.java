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

        mGo_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().goForward(0, 0.2f, mMotionListener);
            }
        });

        mGo_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().goBackward(0, 0.2f, mMotionListener);
            }
        });

        mTurn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().turnLeft(0, 0.2f, mMotionListener);
            }
        });

        mTurn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().turnRight(0, 0.2f, mMotionListener);
            }
        });

        mStop_move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RobotApi.getInstance().stopMove(0, mMotionListener);
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
