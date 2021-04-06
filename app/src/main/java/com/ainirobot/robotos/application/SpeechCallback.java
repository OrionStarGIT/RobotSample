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

package com.ainirobot.robotos.application;

import android.os.RemoteException;

import com.ainirobot.coreservice.client.speech.SkillCallback;
import com.ainirobot.robotos.LogTools;

public class SpeechCallback extends SkillCallback {

    private static final String TAG = SpeechCallback.class.getName();

    @Override
    public void onSpeechParResult(String s) throws RemoteException {
        LogTools.info(TAG +" onSpeechParResult:"+ s);
    }

    @Override
    public void onStart() throws RemoteException {
        LogTools.info(TAG+" onStart");
    }

    @Override
    public void onStop() throws RemoteException {
        LogTools.info(TAG+" onStop");
    }

    @Override
    public void onVolumeChange(int i) throws RemoteException {
        LogTools.info(TAG+" onVolumeChange :" + i);
    }

    @Override
    public void onQueryEnded(int i) throws RemoteException {
        LogTools.info(TAG+" onQueryEnded :" + i);
    }

    @Override
    public void onQueryAsrResult(String asrResult) throws RemoteException {
        LogTools.info(TAG+" onQueryAsrResult :" + asrResult);
    }
}
