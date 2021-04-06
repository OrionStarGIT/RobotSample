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

import androidx.annotation.Nullable;

import com.ainirobot.robotos.MainActivity;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.fragment.MainFragment;

public class BackView extends LinearLayout {

    private Button mBack_btn;
    private Context mContext;
    public BackView(Context context) {
        super(context);
        init(context);
    }

    public BackView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_back_view,this,true);
        mContext  = context;
        bindViews();
    }

    private void bindViews() {
        mBack_btn = (Button) findViewById(R.id.back_btn);
        mBack_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().switchFragment(MainFragment.newInstance());
            }
        });
    }
}
