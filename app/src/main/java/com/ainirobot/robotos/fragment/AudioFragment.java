package com.ainirobot.robotos.fragment;

import android.Manifest;
import android.content.Context;
import android.media.AudioFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.ainirobot.robotos.LogTools;
import com.ainirobot.robotos.R;
import com.ainirobot.robotos.audio.AudioManager;

/**
 * Data: 2023/8/9 19:42
 * Author: wanglijing
 * Description: AudioFragment
 */
public class AudioFragment extends BaseFragment {

    private Button btnStart;
    private Button btnStop;

    public static Fragment newInstance() {
        return new AudioFragment();
    }


    @Override
    public View onCreateView(Context context) {
        View root = mInflater.inflate(R.layout.fragment_audio_layout, null, false);
        requestPermissions(new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        }, 1);
        initView(root);
        return root;
    }

    private void initView(View root) {
        btnStart = root.findViewById(R.id.start_btn);
        btnStop = root.findViewById(R.id.stop_btn);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int bufferSize = 48000;
                AudioManager.getInstance().startRecord(AudioFormat.CHANNEL_IN_MONO, bufferSize, new AudioManager.AudioRecordCallback() {
                    @Override
                    public void onFrameDataIn(byte[] data) {
                        LogTools.info("开始录音 size:" + data.length);
                    }
                });
            }
        });


        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager.getInstance().stopRecord();
                LogTools.info("结束录音 文件已保存到："+AudioManager.TEST_FILE_NAME);
            }
        });
    }

}
