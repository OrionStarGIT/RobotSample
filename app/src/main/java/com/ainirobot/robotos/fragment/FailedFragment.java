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

import com.ainirobot.robotos.R;

import androidx.fragment.app.Fragment;

public class FailedFragment extends BaseFragment {


    private Button mExit;

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_failed_layout,null,false);
        bindViews(root);
        return root;
    }

    private void bindViews(View root) {
        mExit = (Button) root.findViewById(R.id.exit);
        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });
    }

    public static Fragment newInstance() {
        return new FailedFragment();
    }
}
