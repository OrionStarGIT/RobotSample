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

package com.ainirobot.robotos.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;

public class ResultView extends LinearLayout {

    private Button mClear_result;
    private Button mRecovery_result;
    private ScrollView mSc_result;
    private TextView mTv_result;
    public ResultView(Context context) {
        super(context);
        init(context);
    }

    public ResultView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_result_view,this,true);
        bindViews();
        LogTools.addLogListener(new LogTools.OnLogListener() {
            @Override
            public void onLog(String data) {
                mTv_result.setText(data);
            }
        });
    }

    private void bindViews() {
        mClear_result = (Button) findViewById(R.id.clear_result);
        mRecovery_result = (Button) findViewById(R.id.recovery_result);
        mSc_result = (ScrollView) findViewById(R.id.sc_result);
        mTv_result = (TextView) findViewById(R.id.tv_result);

        mClear_result.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LogTools.clearHistory();
                mTv_result.setText("");
            }
        });

        mRecovery_result.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recoveryResult();
            }
        });
    }

    private void recoveryResult(){
        mTv_result.setText(LogTools.getHistoryText());
    }
}
