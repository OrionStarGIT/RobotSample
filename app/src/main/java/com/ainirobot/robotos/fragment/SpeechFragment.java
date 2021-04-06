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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.ainirobot.coreservice.client.listener.TextListener;
import com.ainirobot.coreservice.client.speech.SkillApi;
import com.ainirobot.coreservice.client.speech.entity.TTSEntity;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.application.RobotOSApplication;

public class SpeechFragment extends BaseFragment {

    private Button mPlay_btn;
    private Button mStop_btn;
    private Button mQuery_btn;
    private SkillApi mSkillApi;
    private EditText mEt_play_text;

    @Override
    public View onCreateView(Context context) {
        mSkillApi = RobotOSApplication.getInstance().getSkillApi();
        View root = mInflater.inflate(R.layout.fragment_speech_layout,null,false);
        bindViews(root);
        return root;
    }

    private void bindViews(View root) {
        mPlay_btn = (Button) root.findViewById(R.id.play_btn);
        mStop_btn = (Button) root.findViewById(R.id.stop_btn);
        mQuery_btn = (Button) root.findViewById(R.id.query_btn);
        mEt_play_text = (EditText) root.findViewById(R.id.et_play_text);

        mPlay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEt_play_text.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    text = mEt_play_text.getHint().toString();
                }
                playText(text);
            }
        });

        mStop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTTS();
            }
        });

        mQuery_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEt_play_text.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    text = mEt_play_text.getHint().toString();
                }
                queryByText(text);
            }
        });
    }

    private void playText(String text) {
        if (mSkillApi != null) {
            mSkillApi.playText(new TTSEntity("sid-1234567890", text), mTextListener);
        }
    }

    private TextListener mTextListener = new TextListener() {
        @Override
        public void onStart() {
            super.onStart();
            LogTools.info("onStart");
        }

        @Override
        public void onStop() {
            super.onStop();
            LogTools.info("onStop");
        }

        @Override
        public void onComplete() {
            super.onComplete();
            LogTools.info("onComplete");
        }

        @Override
        public void onError() {
            super.onError();
            LogTools.info("onError");
        }
    };

    private void stopTTS(){
        if(mSkillApi != null){
            mSkillApi.stopTTS();
        }
    }

    private void queryByText(String text){
        if(mSkillApi != null){
            mSkillApi.queryByText(text);
        }
    }

    public static Fragment newInstance() {
        return new SpeechFragment();
    }
}
