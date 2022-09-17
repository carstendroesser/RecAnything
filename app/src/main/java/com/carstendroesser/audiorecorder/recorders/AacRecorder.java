package com.carstendroesser.audiorecorder.recorders;

import android.content.Context;
import android.media.MediaRecorder;

import com.carstendroesser.audiorecorder.utils.Settings;

/**
 * Created by carstendrosser on 26.11.17.
 */

public class AacRecorder extends MediaRecorderBasedRecorder {

    public AacRecorder(Context pContext) {
        super(pContext, ".m4a");
    }

    @Override
    protected void setup() {
        mIsRecording = false;
        mIsPaused = false;
        mMediaRecorder.setAudioSource(Settings.getRecordingSource(mContext));
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioEncodingBitRate(96000);
        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder pMediaRecorder, int pWhat, int pExtra) {
                notifyListenersInMainThread(OnRecordActionListener.Action.RECORD_FAIL);
            }
        });
    }
    
}
