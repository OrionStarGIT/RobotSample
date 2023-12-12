/*
 * Copyright (C) 2017 OrionStar Technology Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ainirobot.robotos.maputils;

import android.content.Context;

import com.ainirobot.robotos.R;


public class DialogUtils {

    private static DialogUtils mInstance;

    public static DialogUtils getInstance() {
        if (null == mInstance) {
            mInstance = new DialogUtils();
        }
        return mInstance;
    }

    public static void showGoNavigation(
            Context context, String placeName, DialogConfirm.ConfirmCallBack confirmCallBack,
            DialogConfirm.CancelBtnCallBack cancelBtnCallBack) {
        final DialogConfirm dialogConfirm = new DialogConfirm(context);
        dialogConfirm.setTextContent1(context.getString(R.string.go_navigation));
        dialogConfirm.setTextContent2(context.getString(R.string.sure_go_navigation, placeName));
        dialogConfirm.setTextCancle(context.getString(R.string.cancel_txt));
        dialogConfirm.setTextConfirm(context.getString(R.string.confirm_txt));
        dialogConfirm.setConfirmCallBack(confirmCallBack);
        dialogConfirm.setCancelBtnCallBack(cancelBtnCallBack);
        dialogConfirm.show();
    }
}
