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

import com.ainirobot.coreservice.client.listener.Person;
import com.ainirobot.coreservice.client.person.PersonApi;
import com.ainirobot.coreservice.client.person.PersonListener;
import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;

import java.util.List;

public class VisionFragment extends BaseFragment {

    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_vision_layout,null,false);
        initViews(root);
        return root;
    }

    public void initViews(View root){
        Button registerBtn = root.findViewById(R.id.register_btn);
        Button unRegisterBtn = root.findViewById(R.id.unregister_btn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerPersonListener();
            }
        });

        unRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonApi.getInstance().unregisterPersonListener(mListener);
            }
        });
    }

    /**
     * 注册人员监听
     */
    private void registerPersonListener(){
        PersonApi.getInstance().registerPersonListener(mListener);
    }


    /**
     * 人员变化时，可以调用获取当前人员列表接口获取机器人视野内所有人员
     */
    private PersonListener mListener = new PersonListener() {
        @Override
        public void personChanged() {
            super.personChanged();
            List<Person> allFaceList = PersonApi.getInstance().getAllPersons();
            LogTools.info(allFaceList.toString());
        }
    };

    /**
     * 取消注册人员监听
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PersonApi.getInstance().unregisterPersonListener(mListener);
    }

    public static Fragment newInstance() {
        return new VisionFragment();
    }
}
