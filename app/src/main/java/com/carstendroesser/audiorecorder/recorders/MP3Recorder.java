package com.carstendroesser.audiorecorder.recorders;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

import com.carstendroesser.audiorecorder.utils.FileFactory;
import com.carstendroesser.audiorecorder.utils.Settings;
import com.carstendroesser.audiorecorder.utils.WritableFile;
import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.IOException;

/**
 * Created by carstendrosser on 12.12.17.
 */

public class MP3Recorder extends AudioBasedRecorder {

    private short[] mBuffer;
    private AndroidLame mAndroidLame;
    private byte[] mMp3Buffer;
    private WritableFile mFile;

    public MP3Recorder(Context pContext) {
        super(pContext);
    }

    private boolean setup(int pAudioSource, int pSampleRate, int pChannelConfig, int pAudioFormat) throws IOException {

        mFilePath = FileFactory.createRecordFilePath(mContext, ".mp3");

        //set samples according to the audioformat
        if (pAudioFormat == AudioFormat.ENCODING_PCM_16BIT) {
            mSamples = 16;
        } else {
            mSamples = 8;
        }

        //set channels according to channel_configuration
        if (pChannelConfig == AudioFormat.CHANNEL_IN_MONO) {
            mChannels = 1;
        } else {
            mChannels = 2;
        }

        mSampleRate = pSampleRate;
        mAmplitude = 0;

        mBufferSize = AudioRecord.getMinBufferSize(pSampleRate, pChannelConfig, pAudioFormat);
        mFramePeriod = mBufferSize / (2 * mSamples * mChannels / 8);
        mAudioRecord = new AudioRecord(pAudioSource, pSampleRate, pChannelConfig, pAudioFormat, mBufferSize);

        mAndroidLame = new LameBuilder()
                .setInSampleRate(mSampleRate)
                .setOutChannels(mChannels)
                .setOutBitrate(192)
                .setOutSampleRate(mSampleRate)
                .build();

        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void pause() {
        mIsPaused = true;
        mBytesInSilence = 0;

        if (mIsInSilence) {
            notifyListenersInMainThread(OnRecordActionListener.Action.SILENT_END);
        }
        mIsInSilence = false;
    }

    @Override
    public boolean isSeekable() {
        return false;
    }

    @Override
    protected void onRecordStart() throws Exception {
        reset();

        mIsRecording = true;
        mIsPaused = false;
        mIsInSilence = false;
        mIsAborted = false;

        //setup recorder
        boolean initialized = setup(
                Settings.getRecordingSource(mContext),
                Settings.getSamplerate(mContext),
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (!initialized) {
            throw new Exception("Recorder couldn't be initialized.");
        }

        if (Settings.isEchoCancelingWanted(mContext)) {
            AcousticEchoCanceler.create(mAudioRecord.getAudioSessionId());
        }

        if (Settings.isNoiseCancelingWanted(mContext)) {
            NoiseSuppressor.create(mAudioRecord.getAudioSessionId());
        }

        if (Settings.isAutogainWanted(mContext)) {
            AutomaticGainControl.create(mAudioRecord.getAudioSessionId());
        }

        prepareFile();

        //start recording
        mAudioRecord.startRecording();
//        mAudioRecord.read(mBuffer, 0, mBufferSize);

        //start amplitude broadcast
        startAmplitudeFeed();
    }

    @Override
    protected void onRecordStop() throws IOException {
        int outputMp3buf = mAndroidLame.flush(mMp3Buffer);

        if (outputMp3buf > 0) {
            mFile.write(mMp3Buffer, 0, outputMp3buf);
        }

        mFile.close();

        super.onRecordStop();
    }

    @Override
    protected void onRecordAbort() throws IOException {
        super.onRecordAbort();
        mFile.close();
    }

    @Override
    protected void onRecordingWhileLoop() {
        Log.d("AHA!", "onrecordingLoop");
        // TODO check for silence, amplitude, maybe payloadsize for length of recording?
        int bytesRead = mAudioRecord.read(mBuffer, 0, mBufferSize) * 2;

        //calculate amplitude
        int currentAmplitude = 0;

        float gainvalue = Settings.getGainValue(mContext);

        if (mSamples == 16) {
            for (int i = 0; i < mBuffer.length; i++) { // 16bit sample size

                float sample = mBuffer[i];

                sample *= gainvalue;

                // Avoid 16-bit-integer overflow when writing back the manipulated data:
                if (sample >= 32767f) {
                    mBuffer[i] = (byte) 0x7FFF;
                } else if (sample <= -32768f) {
                    mBuffer[i] = 0x0000;
                } else {
                    int s = (int) (sample);
                    mBuffer[i] = (short) sample;
                }

                if (currentAmplitude < sample) {
                    currentAmplitude = (int) sample;
                }
                if (sample > mAmplitude) { // Check amplitude
                    mAmplitude = (int) sample;
                }
            }
        } else {
            // 8bit samples
        }

        synchronized (this) {
            if (Settings.isDetectSilenceWanted(mContext)) {
                if (currentAmplitude < Settings.getDetectSilenceGate(mContext)) {
                    //silence
                    mBytesInSilence += bytesRead;
                    if (mBytesInSilence >= (mSampleRate * mSamples * mChannels / 8) * Settings.getDetectSilenceDelayValue(mContext)) {
                        if (!mIsInSilence) {
                            notifyListenersInMainThread(OnRecordActionListener.Action.SILENT_START);
                        }
                        stopAmplitudeFeed();
                        mIsInSilence = true;

                        return;
                    }
                } else {
                    //noise
                    if (mIsInSilence) {
                        notifyListenersInMainThread(OnRecordActionListener.Action.SILENT_END);
                    }
                    mBytesInSilence = 0;
                    startAmplitudeFeed();
                    mIsInSilence = false;
                }
            }

            // write process
            if (bytesRead > 0) {
                int bytesEncoded = mAndroidLame.encode(mBuffer, mBuffer, bytesRead / 2, mMp3Buffer);
                if (bytesEncoded > 0) {
                    try {
                        mFile.write(mMp3Buffer, 0, bytesEncoded);
                        mPayloadSize += bytesRead;
                        notifyListenersInMainThread(OnRecordActionListener.Action.NEW_SIZE);
                    } catch (IOException pException) {
                        pException.printStackTrace();
                        mException = pException;
                        notifyListenersInMainThread(OnRecordActionListener.Action.RECORD_FAIL);
                    }
                }
            }
        }

    }

    @Override
    public long getCurrentSizeInByte() {
        if (mFile != null) {
            try {
                return mFile.length();
            } catch (IOException pException) {
                pException.printStackTrace();
                return -1;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void release() {
        super.release();
        if (mAndroidLame != null) {
            mAndroidLame.close();
            mAndroidLame = null;
        }
        mFile = null;
    }

    private void reset() {
        // free ressources
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }

        if (mAndroidLame != null) {
            mAndroidLame.close();
            mAndroidLame = null;
        }

        mFile = null;
        mFilePath = null;
        mChannels = 0;
        mSamples = 0;
        mSampleRate = 0;
        mAmplitude = 0;
        mBufferSize = 0;
        mFramePeriod = 0;
        mPayloadSize = 0;
        notifyListenersInMainThread(OnRecordActionListener.Action.NEW_SIZE);

        mBuffer = null;
        mMp3Buffer = null;
        mBytesInSilence = 0;
    }

    private void prepareFile() throws IOException {
        // create new instance of file
        mFile = new WritableFile(mFilePath, "rw");

        //init the buffer we will record to and we will read after every framePeriod
        mBuffer = new short[mSampleRate * mChannels];
        mMp3Buffer = new byte[(int) (7200 + mBuffer.length * 2 * 1.25)];

        mPayloadSize = 0;
        notifyListenersInMainThread(OnRecordActionListener.Action.NEW_SIZE);
    }

}
