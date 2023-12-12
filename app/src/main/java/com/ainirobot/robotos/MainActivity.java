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

package com.ainirobot.robotos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.widget.TextView;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.os.Handler;

import com.ainirobot.coreservice.client.RobotApi;
import com.ainirobot.robotos.fragment.FailedFragment;
import com.ainirobot.robotos.fragment.MainFragment;

public class MainActivity extends AppCompatActivity {

    private FrameLayout mContent;

    private static MainActivity mInstance;
    private int checkTimes;

    public static MainActivity getInstance(){
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
        TextView textSplash = findViewById(R.id.text_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkTimes = 0;
                setContentView(R.layout.activity_main);
                initViews();
                mInstance = MainActivity.this;
            }
        }, 1200); // 延迟执行checkInit()

        //setContentView(R.layout.activity_main);
    }

    private void initViews() {
        mContent = findViewById(R.id.container_content);
        checkInit();
    }

    public void switchFragment(Fragment fragment){

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_content, fragment, fragment.getClass().getName())
                .commit();
    }

    private void checkInit(){
        checkTimes++;
        if(checkTimes > 10){
            Fragment fragment = FailedFragment.newInstance();
            switchFragment(fragment);
        }
        else if(RobotApi.getInstance().isApiConnectedService()
                && RobotApi.getInstance().isActive()){
            Fragment fragment = MainFragment.newInstance();
            switchFragment(fragment);
        }else
        {
            mContent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 继续显示Splash界面
                    checkInit();
                }
            },300);
        }
    }
}
