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

import com.ainirobot.robotos.R;

public class MainFragment extends BaseFragment {

    private Button mLead_scene;
    private Button mSport_scene;
    private Button mSpeech_scene;
    private Button mVision_scene;
    private Button mCharge_scene;
    private Button mLocation_scene;
    private Button mNavigation_scene;
    private Button mAudio_scene;
    private Button mExit;
    private Button mXBack;
    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_main_layout,null,false);
        bindViews(root);
        hideBackView();
        hideResultView();
        return root;
    }

    private void bindViews(View root) {
        mLead_scene = (Button) root.findViewById(R.id.lead_scene);
        mSport_scene = (Button) root.findViewById(R.id.sport_scene);
        mSpeech_scene = (Button) root.findViewById(R.id.speech_scene);
        mVision_scene = (Button) root.findViewById(R.id.vision_scene);
        mCharge_scene = (Button) root.findViewById(R.id.charge_scene);
        mLocation_scene = (Button) root.findViewById(R.id.location_scene);
        mNavigation_scene = (Button) root.findViewById(R.id.navigation_scene);
        mAudio_scene = (Button) root.findViewById(R.id.audio_scene);
        mExit = (Button) root.findViewById(R.id.exit);



        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                    getActivity().finish();
                }
            }
        });
        mLead_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(LeadFragment.newInstance());
            }
        });

        mSpeech_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(SpeechFragment.newInstance());
            }
        });

        mSport_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(SportFragment.newInstance());
            }
        });

        mVision_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(VisionFragment.newInstance());
            }
        });

        mLocation_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(LocationFragment.newInstance());
            }
        });

        mNavigation_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            switchFragment(NavigationFragment.newInstance());
//                switchFragment(NavFragment.newInstance());
            }
        });

        mCharge_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(ChargeFragment.newInstance());
            }
        });

        mAudio_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(AudioFragment.newInstance());
            }
        });
    }

    public static Fragment newInstance() {
        return new MainFragment();
    }
}
