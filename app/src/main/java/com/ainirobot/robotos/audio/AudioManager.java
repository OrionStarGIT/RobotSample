package com.ainirobot.robotos.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.ainirobot.robotos.maputils.Singleton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

public class AudioManager {
    private static final String TAG = AudioManager.class.getSimpleName();
    //音频相关成员
    private AudioRecord mAudioRecorder = null;
    private final ReentrantLock mRecLocker = new ReentrantLock();
    private boolean mThreadRun = false;
    private Thread mProducerThread;
    public static String TEST_FILE_NAME = "/sdcard/audio.wav";
    private AudioRecordCallback mCallback;
    private int channelConfig;

    private final int AUDIO_RATE = 48000;
    private int mBufferSize;
    private static Singleton<AudioManager> sSingleton = new Singleton<AudioManager>() {
        @Override
        protected AudioManager create() {
            return new AudioManager();
        }
    };

    private AudioManager() {
    }

    public static AudioManager getInstance() {
        return sSingleton.get();
    }

    public interface AudioRecordCallback {
        void onFrameDataIn(byte[] data);
    }

    public int startRecord(int channelConfig, int bufferSize, AudioRecordCallback callback) {
        Log.i(TAG, "Init Recording 1");
        if (mThreadRun) {
            Log.i(TAG, "recorder has started");
            return 0;
        }
        this.mCallback = callback;
        this.channelConfig = channelConfig;
        this.mBufferSize = bufferSize;
        TEST_FILE_NAME = "/sdcard/audio_" + System.currentTimeMillis() + ".wav";

        int minRecBufSize = AudioRecord.getMinBufferSize(
                AUDIO_RATE, channelConfig,
                AudioFormat.ENCODING_PCM_16BIT);

        int recBufSize = minRecBufSize * 2;

        Log.i(TAG, "min buffer size:" + minRecBufSize);

        // release the object
        if (mAudioRecorder != null) {
            mAudioRecorder.release();
            mAudioRecorder = null;
        }

        try {
            mAudioRecorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    AUDIO_RATE,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT,
                    recBufSize);
        } catch (Exception ex) {
            Log.e(TAG, "open audio recore failed:" + ex.getMessage());
            return -1;
        }

        try {
            mAudioRecorder.startRecording();
        } catch (Exception ex) {
            Log.e(TAG, "start recore failed:" + ex.getMessage());
            return -1;
        }

        Log.i(TAG, "start recore status:" + mAudioRecorder.getState() + "," + mAudioRecorder.getRecordingState());
        if (mAudioRecorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            Log.e(TAG, "start recore status err");
            return -1;
        }

        mThreadRun = true;
        mProducerThread = new Thread(mRunnableRecorder, "AudioProducerThread");
        mProducerThread.start();

        return 0;
    }

    public String stopRecord() {
        Log.i(TAG, "Stop Record");
        mThreadRun = false;
        mRecLocker.lock();
        try {
            if (mAudioRecorder != null && mAudioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                mAudioRecorder.stop();
            }

            if (mAudioRecorder != null) {
                mAudioRecorder.release();
                mAudioRecorder = null;
            }

            if (mProducerThread != null) {
                mProducerThread.join(50);
            }
            mProducerThread = null;
            reWriteWavHeader();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mRecLocker.unlock();
        }

        Log.i(TAG, "Stop Record end:"+TEST_FILE_NAME);
        return TEST_FILE_NAME;
    }

    private void reWriteWavHeader(){
        try{
            RandomAccessFile file = new RandomAccessFile(TEST_FILE_NAME, "rw");
            file.seek(0);
            WavHeader header = new WavHeader((int)file.length(),
                    AUDIO_RATE,
                    (short)1,(short)16);
            file.write(header.getHeader());
            file.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void saveData(byte[] bytes) {
        try {
            OutputStream os;
            File f = new File(TEST_FILE_NAME);
            if(f.exists()){
                os = new FileOutputStream(TEST_FILE_NAME, true);
                os.write(bytes);
                os.flush();
                os.close();
            }else{
                os = new FileOutputStream(TEST_FILE_NAME, false);
                WavHeader header = new WavHeader(bytes.length,
                        AUDIO_RATE,
                        (short)1,(short)16);
                os.write(header.getHeader());
                os.write(bytes);
                os.flush();
                os.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Runnable mRunnableRecorder = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "===== Audio Recorder Start ===== ");

            byte[] tempBufRec = new byte[mBufferSize];
            byte[] bfOutLeft = new byte[channelConfig == AudioFormat.CHANNEL_IN_STEREO ? mBufferSize / 2 : mBufferSize];
            byte[] bfOutRight = new byte[channelConfig == AudioFormat.CHANNEL_IN_STEREO ? mBufferSize / 2 : mBufferSize];
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            while (mThreadRun && mAudioRecorder != null) {
                try {
                    int readBytes = mAudioRecorder.read(tempBufRec, 0, mBufferSize);
                    if (readBytes == mBufferSize) {
                        if (channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
                            deinterleaveData(tempBufRec, bfOutLeft, bfOutRight, mBufferSize);
                        } else if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
                            System.arraycopy(tempBufRec, 0, bfOutLeft, 0, mBufferSize);
                        }
                        if (mCallback != null) {
                            mCallback.onFrameDataIn(bfOutLeft);
                        }
                        //TODO:保存到SD卡只是为了查看录制音频是否正确，正式使用建议删除该代码
                        saveData(bfOutLeft);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Record Audio try failed: " + e.getMessage());
                }
            }

            Log.i(TAG, "===== Audio Recorder Stop ===== ");
        }
    };

    private void deinterleaveData(byte[] src, byte[] leftdest, byte[] rightdest, int len) {
        int lIndex = 0;
        int rIndex = 0;
        for (int i = 0; i < len; i += 4) {
            leftdest[lIndex++] = src[i];
            leftdest[lIndex++] = src[i + 1];

            rightdest[rIndex++] = src[i + 2];
            rightdest[rIndex++] = src[i + 3];
        }
    }
}
