package com.carstendroesser.audiorecorder.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.cloud.DropboxHelper;
import com.carstendroesser.audiorecorder.events.RecorderChangedEvent;
import com.carstendroesser.audiorecorder.recorders.AacRecorder;
import com.carstendroesser.audiorecorder.recorders.MP3Recorder;
import com.carstendroesser.audiorecorder.recorders.OnRecordActionListener;
import com.carstendroesser.audiorecorder.recorders.Recorder;
import com.carstendroesser.audiorecorder.recorders.TgppRecorder;
import com.carstendroesser.audiorecorder.recorders.WavRecorder;
import com.carstendroesser.audiorecorder.utils.AudioUtils;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;
import com.carstendroesser.audiorecorder.utils.FormatUtils;
import com.carstendroesser.audiorecorder.utils.NotificationFactory;
import com.carstendroesser.audiorecorder.utils.PermissionsChecker;
import com.carstendroesser.audiorecorder.utils.Preferences;
import com.carstendroesser.audiorecorder.utils.RecordPreferences;
import com.carstendroesser.audiorecorder.utils.Settings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileInputStream;

import static com.carstendroesser.audiorecorder.utils.NotificationFactory.NOTIFICATION_ID;
import static com.carstendroesser.audiorecorder.utils.Settings.isSilentRingerModeWanted;

/**
 * Created by carstendrosser on 10.10.15.
 */
public class RecordService extends Service implements OnRecordActionListener {

    private Recorder mRecorder;
    private String mNotificationMessage;
    private String mOldTime;

    @Override
    public void onCreate() {
        mNotificationMessage = getApplicationContext().getString(R.string.notification_text_ready);
        startForeground(NOTIFICATION_ID,
                NotificationFactory.getNotificationWithText(getApplicationContext(), mNotificationMessage));

        int recorder = Settings.getChoosenRecorder(getApplicationContext());

        switch (recorder) {
            case 0:
                mRecorder = new WavRecorder(getApplicationContext());
                break;
            case 1:
                mRecorder = new MP3Recorder(getApplicationContext());
                break;
            case 2:
                mRecorder = new AacRecorder(getApplicationContext());
                break;
            case 3:
                mRecorder = new TgppRecorder(getApplicationContext());
                break;
        }

        mRecorder.setServiceOnRecordActionListener(this);

        EventBus.getDefault().register(this);

        // battery changes
        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {
                if (Settings.isPauseOnLowBatteryWanted(pContext)) {
                    // Are we charging / charged?
                    int status = pIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;

                    if (isCharging) {
                        return;
                    }

                    int level = pIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = pIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    float batteryPct = level / (float) scale;

                    if (batteryPct <= 0.05 && mRecorder.isRecording()) {
                        mRecorder.stop();
                    }
                }
            }
        };

        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryIntentFilter);

        String dropboxToken = Preferences.getDropboxToken(getApplicationContext());

        if (dropboxToken != null) {
            DropboxHelper.init(getApplicationContext(), dropboxToken);
        }
    }

    public Recorder getRecorder() {
        return mRecorder;
    }

    @Subscribe
    public void onEvent(RecorderChangedEvent pEvent) {
        OnRecordActionListener activityListener = mRecorder.getActivityOnRecordActionListener();

        mRecorder.setActivityOnRecordActionListener(null);
        mRecorder.setServiceOnRecordActionListener(null);
        mRecorder.release();

        switch (pEvent.getRecorder()) {
            case 0:
                mRecorder = new WavRecorder(getApplicationContext());
                break;
            case 1:
                mRecorder = new MP3Recorder(getApplicationContext());
                break;
            case 2:
                mRecorder = new AacRecorder(getApplicationContext());
                break;
            case 3:
                mRecorder = new TgppRecorder(getApplicationContext());
                break;
        }

        mRecorder.setActivityOnRecordActionListener(activityListener);
        mRecorder.setServiceOnRecordActionListener(this);
    }

    @Override
    public void onRecordStartIntent() {
        if (isSilentRingerModeWanted(getApplicationContext())) {
            if(PermissionsChecker.hasDoNotDisturbPermission(getApplicationContext())) {
                AudioUtils.turnToSilentRingerMode(getApplicationContext());
            } else {
                com.carstendroesser.audiorecorder.utils.Settings.setSilentRingerModeWanted(getApplicationContext(), false);
                Toast.makeText(getApplicationContext(), "Missing permissions! Do-not-disturb setting was disabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRecordStartSuccessful() {
        updateNotification(getString(R.string.notification_text_started), true);
    }

    @Override
    public void onRecordFail(Exception pException) {
        updateNotification(getString(R.string.notification_text_fail), false);
        if (isSilentRingerModeWanted(getApplicationContext())) {
            if(PermissionsChecker.hasDoNotDisturbPermission(getApplicationContext())) {
                AudioUtils.restoreSavedRingerMode(getApplicationContext());
            } else {
                com.carstendroesser.audiorecorder.utils.Settings.setSilentRingerModeWanted(getApplicationContext(), false);
                Toast.makeText(getApplicationContext(), "Missing permissions! Do-not-disturb setting was disabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRecordStopIntent() {
    }

    @Override
    public void onRecordStopSuccessful(final String pFilePath) {
        updateNotification(getString(R.string.notification_text_ready), false);
        if (isSilentRingerModeWanted(getApplicationContext())) {
            if(PermissionsChecker.hasDoNotDisturbPermission(getApplicationContext())) {
                AudioUtils.restoreSavedRingerMode(getApplicationContext());
            } else {
                com.carstendroesser.audiorecorder.utils.Settings.setSilentRingerModeWanted(getApplicationContext(), false);
                Toast.makeText(getApplicationContext(), "Missing permissions! Do-not-disturb setting was disabled.", Toast.LENGTH_SHORT).show();
            }
        }

        final String name = pFilePath.substring(pFilePath.lastIndexOf("/") + 1, pFilePath.lastIndexOf("."));
        final String extension = pFilePath.substring(pFilePath.lastIndexOf("."), pFilePath.length());
        long id = DatabaseHelper.getInstance(this).insertRecord(pFilePath, name);

        //set this id as last record
        RecordPreferences.putLastRecordId(getApplicationContext(), (int) id);

        if (Settings.isMediaStoreWanted(getApplicationContext())) {
            AudioUtils.addToMediaStore(getApplicationContext(), name, pFilePath);
        }

        if (Settings.isAutouploadWanted(getApplicationContext()) && DropboxHelper.isInitialized()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        DropboxHelper.getClient().files().uploadBuilder("/" + name + extension).uploadAndFinish(new FileInputStream(new File(pFilePath)));
                    } catch (Exception pException) {
                        pException.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onRecordPauseIntent() {
    }

    @Override
    public void onRecordResumeIntent() {
    }

    @Override
    public void onRecordPaused() {
        updateNotification(getString(R.string.notification_text_paused), true);
    }

    @Override
    public void onRecordResumed() {
        updateNotification(getString(R.string.notification_text_started), true);
    }

    @Override
    public void onNewAmplitude(int pAmplitude) {
    }

    @Override
    public void onSilenceStart() {
        updateNotification(getString(R.string.notification_text_skipping_silence), true);
    }

    @Override
    public void onSilenceEnd() {
        updateNotification(getString(R.string.notification_text_started), true);
    }

    @Override
    public void onSeek() {
    }

    @Override
    public void onSeekEnd() {
    }

    @Override
    public void onSeekLimitReached() {
    }

    @Override
    public void onNewSize(long pSizeInByte) {
        String time = FormatUtils.toReadableDuration(mRecorder.getTotalTime());

        if (!time.equals(mOldTime) && !mRecorder.isInSilence()) {
            mOldTime = time;
            updateNotification(mNotificationMessage, true);
        }
    }

    @Override
    public void onRecordAbortIntent() {

    }

    @Override
    public void onRecordAbortSuccess() {
        updateNotification(getString(R.string.notification_text_ready), false);
        if (isSilentRingerModeWanted(getApplicationContext())) {
            if(PermissionsChecker.hasDoNotDisturbPermission(getApplicationContext())) {
                AudioUtils.restoreSavedRingerMode(getApplicationContext());
            } else {
                com.carstendroesser.audiorecorder.utils.Settings.setSilentRingerModeWanted(getApplicationContext(), false);
                Toast.makeText(getApplicationContext(), "Missing permissions! Do-not-disturb setting was disabled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public IBinder onBind(Intent pIntent) {
        return new ServiceBinder();
    }

    private synchronized void updateNotification(String pText, boolean pWithDuration) {
        mNotificationMessage = pText;

        if (pWithDuration == true) {
            pText = FormatUtils.toReadableDuration(mRecorder.getTotalTime()) + " / " + pText;
        }

        ((NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, NotificationFactory.getNotificationWithText(getApplicationContext(), pText));
    }

    public class ServiceBinder extends Binder {
        public RecordService getService() {
            return RecordService.this;
        }
    }

}
