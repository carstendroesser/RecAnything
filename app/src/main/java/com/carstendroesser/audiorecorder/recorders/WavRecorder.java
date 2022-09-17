package com.carstendroesser.audiorecorder.recorders;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;

import com.carstendroesser.audiorecorder.utils.FileFactory;
import com.carstendroesser.audiorecorder.utils.Settings;
import com.carstendroesser.audiorecorder.utils.WavFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by carstendrosser on 20.01.16.
 */
public class WavRecorder extends AudioBasedRecorder {

    //MEMBERS

    private volatile boolean mIsSeeking;
    private WavFile mWavFile;
    private byte[] mBuffer;

    //THE WHOLE THING

    /**
     * Creates a new WavRecorder. Needs to call super to setup everything correctly.
     */
    public WavRecorder(Context pContext) {
        super(pContext);
    }

    //ACTIONS

    @Override
    protected void onRecordingThreadFinally() {
        super.onRecordingThreadFinally();
        mIsSeeking = false;
    }

    @Override
    protected void onRecordStart() throws Exception {
        //reset recorder
        reset();

        mIsRecording = true;
        mIsPaused = false;
        mIsInSilence = false;
        mIsSeeking = false;
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
        mAudioRecord.read(mBuffer, 0, mBuffer.length);

        //start amplitude broadcast
        startAmplitudeFeed();
    }

    @Override
    protected void onRecordStop() throws IOException {
        super.onRecordStop();
        mWavFile.writeFilesize(36 + mPayloadSize);
        mWavFile.writeDataSize(mPayloadSize);
        mWavFile.close();
    }

    @Override
    protected void onRecordAbort() throws IOException {
        super.onRecordAbort();
        mWavFile.close();
        File file = new File(mWavFile.getPath());
    }

    @Override
    protected void onRecordingWhileLoop() {
        if (mIsSeeking) {
            return;
        }

        //fill the buffer
        mAudioRecord.read(mBuffer, 0, mBuffer.length);
        try {
            //calculate amplitude
            int currentAmplitude = 0;

            float gainvalue = Settings.getGainValue(mContext);

            if (mSamples == 16) {
                for (int i = 0; i < mBuffer.length / 2; i++) { // 16bit sample size

                    float sample = (float) (mBuffer[i * 2] & 0xFF | mBuffer[i * 2 + 1] << 8);

                    sample *= gainvalue;

                    // Avoid 16-bit-integer overflow when writing back the manipulated data:
                    if (sample >= 32767f) {
                        mBuffer[i * 2] = (byte) 0xFF;
                        mBuffer[i * 2 + 1] = 0x7F;
                    } else if (sample <= -32768f) {
                        mBuffer[i * 2] = 0x00;
                        mBuffer[i * 2 + 1] = (byte) 0x80;
                    } else {
                        int s = (int) (sample);
                        mBuffer[i * 2] = (byte) (s & 0xFF);
                        mBuffer[i * 2 + 1] = (byte) (s >> 8 & 0xFF);
                    }

                    if (currentAmplitude < sample) {
                        currentAmplitude = (int) sample;
                    }
                    if (sample > mAmplitude) { // Check amplitude
                        mAmplitude = (int) sample;
                    }
                }
            } else { // 8bit sample size
                for (int i = 0; i < mBuffer.length; i++) {
                    if (currentAmplitude < mBuffer[i]) {
                        currentAmplitude = mBuffer[i];
                    }
                    if (mBuffer[i] > mAmplitude) { // Check amplitude
                        mAmplitude = mBuffer[i];
                    }
                }
            }

            synchronized (this) {
                if (Settings.isDetectSilenceWanted(mContext)) {
                    if (currentAmplitude < Settings.getDetectSilenceGate(mContext)) {
                        //silence
                        mBytesInSilence += mBuffer.length;
                        if (mBytesInSilence >= mWavFile.getByterate() * Settings.getDetectSilenceDelayValue(mContext)) {
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


                //write buffer to file
                mWavFile.appendBytes(mBuffer);

                //recalculate the final filesize
                mPayloadSize += mBuffer.length;
                notifyListenersInMainThread(OnRecordActionListener.Action.NEW_SIZE);
            }

        } catch (IOException pException) {
        }
    }

    //HELPERS

    private void reset() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }

        mWavFile = null;
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

        mBytesInSilence = 0;
    }

    private void prepareFile() throws IOException {
        //access the file to write to
        mWavFile = new WavFile(mFilePath, "rw");
        mWavFile.writeHeaderData();
        mWavFile.writeChannels(mChannels);
        mWavFile.writeSampleRate(mSampleRate);
        mWavFile.writeByteRate(mSampleRate * mSamples * mChannels / 8);
        mWavFile.writeBlockAlign((short) (mChannels * mSamples / 8));
        mWavFile.writeBitsPerSample(mSamples);

        //init the buffer we will record to and we will read after every framePeriod
        mBuffer = new byte[mFramePeriod * mSamples / 8 * mChannels];
        mPayloadSize = 0;
        notifyListenersInMainThread(OnRecordActionListener.Action.NEW_SIZE);
    }

    private boolean setup(int pAudioSource, int pSampleRate, int pChannelConfig, int pAudioFormat) throws IOException {

        mFilePath = FileFactory.createRecordFilePath(mContext, ".wav");

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

        //calculate buffersize
        mBufferSize = AudioRecord.getMinBufferSize(pSampleRate, pChannelConfig, pAudioFormat);

        mFramePeriod = mBufferSize / (2 * mSamples * mChannels / 8);

        mAudioRecord = new AudioRecord(pAudioSource, pSampleRate, pChannelConfig, pAudioFormat, mBufferSize);

        mAmplitude = 0;

        if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            return true;
        } else {
            return false;
        }
    }

    //RECORDER

    @Override
    public void stop() {
        super.stop();
        mIsSeeking = false;
    }

    @Override
    public void pause() {
        mIsPaused = true;
        mBytesInSilence = 0;
        mIsSeeking = false;

        if (mIsInSilence) {
            notifyListenersInMainThread(OnRecordActionListener.Action.SILENT_END);
        }
        mIsInSilence = false;
    }

    @Override
    public boolean isSeekable() {
        return true;
    }

    /**
     * Seeks the current recording a specified delta back.
     * OnRecordActionListener is called with action SEEK.
     */
    @Override
    public void seekBack() {
        notifyListenersInMainThread(OnRecordActionListener.Action.SEEK);

        mIsSeeking = true;
        stopAmplitudeFeed();

        if (mIsInSilence) {
            notifyListenersInMainThread(OnRecordActionListener.Action.SILENT_END);
            mBytesInSilence = 0;
            mIsInSilence = false;
        }

        synchronized (this) {
            try {
                int byteToDelete = mWavFile.getFramesPerSecond() * Settings.getSeekValue(mContext) * mWavFile.getFrameSize();
                if (byteToDelete < mPayloadSize) {
                    mPayloadSize -= byteToDelete;
                    notifyListenersInMainThread(OnRecordActionListener.Action.NEW_SIZE);
                    mWavFile.setLength(mWavFile.length() - byteToDelete);
                } else {
                    notifyListenersInMainThread(OnRecordActionListener.Action.SEEK_LIMIT_REACHED);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The seeking has finished. The amplitudefeed is started again.
     */
    @Override
    public void onSeekEnd() {
        notifyListenersInMainThread(OnRecordActionListener.Action.SEEK_END);
        mIsSeeking = false;
        if (!isPaused()) {
            startAmplitudeFeed();
        }
    }

    @Override
    public long getCurrentSizeInByte() {
        return mPayloadSize + 44;
    }

    @Override
    public void release() {
        super.release();
        mWavFile = null;
    }

}
