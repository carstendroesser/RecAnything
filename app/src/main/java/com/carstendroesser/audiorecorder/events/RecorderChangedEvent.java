package com.carstendroesser.audiorecorder.events;

/**
 * Created by carstendrosser on 26.11.17.
 */

public class RecorderChangedEvent {

    // 0 = wav, 1 = aac, 2 = 3gpp
    private int mRecorder;

    public RecorderChangedEvent(int pRecorder) {
        this.mRecorder = pRecorder;
    }

    public int getRecorder() {
        return mRecorder;
    }

}
