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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * LogTools
 *
 * @author Orion
 */
public class LogTools {

    private static final String TAG = LogTools.class.getName();

    private static List<OnLogListener> mAllListener = new ArrayList<>();

    private static Handler mMainHandler = new Handler(Looper.getMainLooper());

    private static StringBuilder mBuilder = new StringBuilder();

    public static void addLogListener(OnLogListener listener) {
        if (!mAllListener.contains(listener)) {
            mAllListener.add(listener);
        }
    }

    public static void info(String data) {
        Log.i(TAG, data);
        mBuilder.append(data + "\r\n");
        notifyListener(mBuilder.toString());
    }

    private static void notifyListener(final String data) {
        if (mAllListener.size() > 0) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (OnLogListener listener : mAllListener) {
                        listener.onLog(data);
                    }
                }
            });
        }
    }

    public static void clearHistory() {
        mBuilder.delete(0, mBuilder.length());
    }

    public static String getHistoryText(){
        return mBuilder.toString();
    }

    public interface OnLogListener {
        void onLog(String data);
    }
}
