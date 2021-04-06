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
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.MainActivity;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.view.BackView;
import com.ainirobot.robotos.view.ResultView;

public abstract class BaseFragment extends Fragment {

    private BackView mBv_back;
    private ResultView mRv_result;
    private RelativeLayout mRl_content;

    protected MainActivity mActivity;
    protected LayoutInflater mInflater;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
        }
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_basic_layout, container, false);
        bindViews(root);
        return root;
    }

    private void bindViews(View root) {
        mBv_back = (BackView) root.findViewById(R.id.bv_back);
        mRl_content = (RelativeLayout) root.findViewById(R.id.rl_content);
        mRv_result = (ResultView) root.findViewById(R.id.rv_result);
        mRl_content.addView(onCreateView(mActivity));
    }

    protected void showBackView() {
        mBv_back.setVisibility(View.VISIBLE);
    }

    protected void hideBackView() {
        mBv_back.setVisibility(View.GONE);
    }

    protected void showResultView() {
        mRv_result.setVisibility(View.VISIBLE);
    }

    protected void hideResultView() {
        mRv_result.setVisibility(View.GONE);
    }

    public void switchFragment(Fragment fragment){
        mActivity.switchFragment(fragment);
    }

    public abstract View onCreateView(Context context);


    @Override
    public void onStop() {
        super.onStop();
        LogTools.clearHistory();
    }
}
