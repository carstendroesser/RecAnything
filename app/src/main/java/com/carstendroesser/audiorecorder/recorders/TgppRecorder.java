package com.carstendroesser.audiorecorder.recorders;

import android.content.Context;
import android.media.MediaRecorder;

import com.carstendroesser.audiorecorder.utils.Settings;

/**
 * Created by carstendrosser on 26.11.17.
 */

public class TgppRecorder extends MediaRecorderBasedRecorder {

    public TgppRecorder(Context pContext) {
        super(pContext, ".3gp");
    }

    @Override
    protected void setup() {
        mIsRecording = false;
        mIsPaused = false;
        mMediaRecorder.setAudioSource(Settings.getRecordingSource(mContext));
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setAudioSamplingRate(8000);
        mMediaRecorder.setAudioEncodingBitRate(12200);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder pMediaRecorder, int pWhat, int pExtra) {
                notifyListenersInMainThread(OnRecordActionListener.Action.RECORD_FAIL);
            }
        });
    }

}
