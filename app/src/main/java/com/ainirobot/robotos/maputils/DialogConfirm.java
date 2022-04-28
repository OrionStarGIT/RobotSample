package com.ainirobot.robotos.maputils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ainirobot.robotos.R;

public class DialogConfirm extends AlertDialog {

    private Context context;
    private TextView content1;
    private TextView content2;
    private TextView tv_cancle_creat_map;
    private  TextView tv_reset;

    private String textContent1 = "";
    private String textContent2 = "";
    private String textCancle = "";
    private String textSure = "";
    private ImageView closeDialog;
    private int dialogHeight = 462;
    private boolean dismissAbleWhenClickOutSide = false;
    private int leftBtnColor;

    public DialogConfirm(Context context) {
        super(context);
        this.context = context;
        leftBtnColor = context.getColor(R.color.dialog_reset_left);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view =  LayoutInflater.from(context).inflate(R.layout.dialog_reset, null);
        setContentView(view);
        Window win = getWindow();

        WindowManager.LayoutParams lp = win.getAttributes();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = 940;
        lp.gravity = Gravity.CENTER;
        win.setDimAmount(0.7f);
        setCanceledOnTouchOutside(dismissAbleWhenClickOutSide);

        content1 = (TextView) view.findViewById(R.id.content1);
        content2 = (TextView) view.findViewById(R.id.content2);
        tv_cancle_creat_map = (TextView) view.findViewById(R.id.tv_cancle_creat_map);
        tv_reset = (TextView) view.findViewById(R.id.tv_reset);
        if (TextUtils.isEmpty(textContent1)) {
            content1.setVisibility(View.GONE);
        } else {
            content1.setText(textContent1);
        }
        content2.setText(textContent2);
        tv_cancle_creat_map.setText(textCancle);
        tv_cancle_creat_map.setTextColor(leftBtnColor);
        tv_reset.setText(textSure);

        tv_cancle_creat_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               dismiss();
                try {
                    cancelBtnCallBack.cancelClick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        tv_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                try {
                    confirmCallBack.confirmClick();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public int getDialogHeight() {
        return dialogHeight;
    }

    public void setDialogHeight(int dialogHeight) {
        this.dialogHeight = dialogHeight;
    }

    public String getTextContent1() {
        return textContent1;
    }

    public void setTextContent1(String textContent1) {
        this.textContent1 = textContent1;
    }

    public String getTextContent2() {
        return textContent2;
    }

    public void setTextContent2(String textContent2) {
        this.textContent2 = textContent2;
    }

    public String getTextCancle() {
        return textCancle;
    }

    public void setTextCancle(String textCancle) {
        this.textCancle = textCancle;
    }

    public String getTextSure() {
        return textSure;
    }

    public void setTextConfirm(String textSure) {
        this.textSure = textSure;
    }

    private ConfirmCallBack confirmCallBack;

    public void setConfirmCallBack(ConfirmCallBack callBack){

        this.confirmCallBack = callBack;
    }

    public interface ConfirmCallBack {
        void confirmClick();
    }

    private CancelBtnCallBack cancelBtnCallBack;

    public void setCancelBtnCallBack(CancelBtnCallBack cancelBtnCallBack) {
        this.cancelBtnCallBack = cancelBtnCallBack;
    }

    public interface CancelBtnCallBack {
        void cancelClick();
    }

}
