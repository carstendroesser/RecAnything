package com.carstendroesser.audiorecorder.activities;

import android.Manifest;
import android.animation.LayoutTransition;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.carstendroesser.audiorecorder.Dialogs.ConfirmDialog;
import com.carstendroesser.audiorecorder.Dialogs.FilterDeletedDialog;
import com.carstendroesser.audiorecorder.Dialogs.FilterDialog;
import com.carstendroesser.audiorecorder.Dialogs.MessageDialog;
import com.carstendroesser.audiorecorder.Dialogs.OkDialog;
import com.carstendroesser.audiorecorder.Dialogs.QuickSettingsDialog;
import com.carstendroesser.audiorecorder.Dialogs.RateAppDialog;
import com.carstendroesser.audiorecorder.Dialogs.RenameDialog;
import com.carstendroesser.audiorecorder.Dialogs.WifiTransferDialog;
import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.fragments.RecordingsFragment;
import com.carstendroesser.audiorecorder.models.Record;
import com.carstendroesser.audiorecorder.recorders.OnRecordActionListener;
import com.carstendroesser.audiorecorder.services.RecordService;
import com.carstendroesser.audiorecorder.services.RecordService.ServiceBinder;
import com.carstendroesser.audiorecorder.utils.AnimationFactory;
import com.carstendroesser.audiorecorder.utils.Constants;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper;
import com.carstendroesser.audiorecorder.utils.DatabaseHelper.Sorting;
import com.carstendroesser.audiorecorder.utils.FormatUtils;
import com.carstendroesser.audiorecorder.utils.IntentFactory;
import com.carstendroesser.audiorecorder.utils.KeyboardHandler;
import com.carstendroesser.audiorecorder.utils.PermissionUtils;
import com.carstendroesser.audiorecorder.utils.Preferences;
import com.carstendroesser.audiorecorder.utils.RecordPreferences;
import com.carstendroesser.audiorecorder.utils.StorageUtils;
import com.carstendroesser.audiorecorder.views.AmplitudeView;
import com.carstendroesser.audiorecorder.views.RevealFrameLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import static android.view.View.GONE;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.carstendroesser.audiorecorder.utils.Settings.getDetectSilenceGate;
import static com.carstendroesser.audiorecorder.utils.Settings.isConfirmAbortWanted;
import static com.carstendroesser.audiorecorder.utils.Settings.isDetectSilenceWanted;
import static com.carstendroesser.audiorecorder.utils.Settings.isKeepScreenOnWanted;
import static com.carstendroesser.audiorecorder.utils.Settings.isQuickRenamingWanted;
import static com.carstendroesser.audiorecorder.utils.Settings.isSeekingWanted;
import static com.carstendroesser.audiorecorder.views.RevealFrameLayout.Position;

public class MainActivity extends AppCompatActivity implements OnRecordActionListener, ServiceConnection, RecordingsFragment.OnRecordingsChangedListener, RecordingsFragment.OnNoResultsResetButtonClickListener, BillingClientStateListener, PurchasesUpdatedListener {

    enum ActivityState {
        CREATED, RESUMED, STARTED, PAUSED, STOPPED, RESTARTED, DESTROYED
    }

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.premiumButton)
    Button mPremiumButton;
    @Bind(R.id.chronometer)
    Chronometer mChronometer;
    @Bind(R.id.amplitudeView)
    AmplitudeView mAmplitudeView;
    @Bind(R.id.recordingsAbortButton)
    ImageView mAbortButton;
    @Bind(R.id.recordingsStartStopButton)
    FloatingActionButton mStartStopButton;
    @Bind(R.id.recordingsPauseResumeButton)
    ImageView mPauseResumeButton;
    @Bind(R.id.silenceIndicatorTextView)
    TextView mSilenceIndicatorTextView;
    @Bind(R.id.seekLimitReachedIndicatorTextView)
    TextView mSeekLimitReachedIndicatorTextView;
    @Bind(R.id.recordingScreenLayout)
    RevealFrameLayout mRecordingScreenLayout;
    @Bind(R.id.bottomBarLayout)
    View mBottomBarLayout;
    @Bind(R.id.searchCloseButton)
    ImageView mSearchCloseButton;
    @Bind(R.id.searchEditText)
    EditText mSearchEditText;
    @Bind(R.id.searchRevealFrameLayout)
    RevealFrameLayout mSearchRevealFrameLayout;
    @Bind(R.id.realtimeinfoTextView)
    TextView mRealTimeInfoTextView;
    @Bind(R.id.recordingviewsContainer)
    LinearLayout mRecordingViewsContainer;

    private ActivityState mActivityState;
    private boolean mBound = false;
    private RecordService mRecordService;
    private RecordingsFragment mRecordingsFragment;
    private BillingClient mBillingClient;
    private boolean mPermissionDialogsShown;

    private static final int REQUESTCODE_CATEGORIES = 1;
    private static final int REQUESTCODE_PERMISSIONS = 2;

    private static final int APP_OPENINGS_SHOW_RATE_DIALOG = 5;

    private TextWatcher mSearchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence pChars, int pStart, int pCount, int pAfter) {
        }

        @Override
        public void onTextChanged(CharSequence pChars, int pStart, int pBefore, int pCount) {
            if (pChars.toString() == "") {
                mRecordingsFragment.filter(null);
            } else {
                mRecordingsFragment.filter(pChars.toString());
            }
        }

        @Override
        public void afterTextChanged(Editable pEditable) {
        }
    };

    // ACTIVITY

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        mActivityState = ActivityState.CREATED;

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mPremiumButton.setVisibility(GONE);

        //check the permissions
        mPermissionDialogsShown = PermissionUtils.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                PermissionUtils.REQUEST_CODE);

        //use toolbar as actionbar
        mToolbar.setTitle("RecAnything");
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);

        mRecordingScreenLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (mBound && mRecordService != null && mRecordService.getRecorder().isRecording()) {
                            // empty
                        } else {
                            mRecordingScreenLayout.setVisibility(GONE);
                        }
                        mRecordingScreenLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        mSearchRevealFrameLayout.post(new Runnable() {
            @Override
            public void run() {
                mSearchRevealFrameLayout.setVisibility(GONE);
            }
        });

        mSearchEditText.addTextChangedListener(mSearchTextWatcher);

        setRecordingViewsEnabled(false);

        //start service
        Intent intent = new Intent(this, RecordService.class);
        startService(intent);

        mRecordingsFragment = (RecordingsFragment) getSupportFragmentManager().findFragmentById(R.id.recordingsFragment);
        mRecordingsFragment.setOnRecordingsChangedListener(this);
        mRecordingsFragment.setOnNoResultsResetButtonClickListener(this);
        mRecordingsFragment.loadRecordings(null);

        //fix position of AmplitudeView, bacause anchorView doesn't work correctly
        //and keeps the amplitudeview within screenboundaries
        int amplitudeViewHeight = mAmplitudeView.getLayoutParams().height;
        int bottomBarHeight = mBottomBarLayout.getLayoutParams().height;
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAmplitudeView.getLayoutParams();
        params.setMargins(0, 0, 0, -(amplitudeViewHeight / 2 - bottomBarHeight));
        mAmplitudeView.setLayoutParams(params);

        Preferences.setAppStartingCounts(this, Preferences.getAppStartingCounts(this) + 1);

        if (mPermissionDialogsShown) {

        } else if (Preferences.getAppStartingCounts(this) >= APP_OPENINGS_SHOW_RATE_DIALOG && !Preferences.getRateAppDialogWasShown(this)) {
            new RateAppDialog(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(IntentFactory.getMarketIntent(MainActivity.this));
                }
            }).show();
            Preferences.setRateAppDialogWasShown(this);
        }

        if (Preferences.getIsProVersion(this)) {
            mPremiumButton.setVisibility(GONE);
            findViewById(R.id.freeversionMessageView).setVisibility(GONE);
        } else {
            mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
            mBillingClient.startConnection(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityState = ActivityState.RESUMED;

        if (Preferences.getIsProVersion(this)) {
            mPremiumButton.setVisibility(GONE);
            findViewById(R.id.freeversionMessageView).setVisibility(GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mActivityState = ActivityState.STARTED;

        //bind the service to have a connection to it
        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityState = ActivityState.PAUSED;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mActivityState = ActivityState.STOPPED;

        if (mBound && mRecordService != null && !mRecordService.getRecorder().isRecording()) {
            stopService(new Intent(MainActivity.this, RecordService.class));
        }

        unbindService();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mActivityState = ActivityState.RESTARTED;

        //start service
        Intent intent = new Intent(this, RecordService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            getApplicationContext().startService(intent);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityState = ActivityState.DESTROYED;
        unbindService();
    }

    @Override
    public void onRequestPermissionsResult(int pRequestCode, String[] pPermissions,
                                           int[] pGrantResults) {
        super.onRequestPermissionsResult(pRequestCode, pPermissions, pGrantResults);

        switch (pRequestCode) {
            case PermissionUtils.REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (pGrantResults.length > 0) {
                    for (int result : pGrantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            //permissions not granted successfully
                            new MessageDialog(this, R.string.dialog_permission_error_title, R.string.dialog_permission_error_message, R.string.dialog_permission_error_close, R.string.dialog_permission_error_settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface pDialog, int pWhich) {
                                    stopService(new Intent(MainActivity.this, RecordService.class));
                                    finish();
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface pDialog, int pWhich) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, REQUESTCODE_PERMISSIONS);
                                    pDialog.dismiss();
                                }
                            }).show();

                            return;
                        }
                    }
                }
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent pEvent) {
        if (isSeekingWanted(getApplicationContext())) {
            if (pEvent.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN && mBound && mRecordService != null
                    && mRecordService.getRecorder().isRecording()
                    && mRecordService.getRecorder().isSeekable()) {

                if (pEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    mRecordService.getRecorder().seekBack();
                } else if (pEvent.getAction() == KeyEvent.ACTION_UP) {
                    mRecordService.getRecorder().onSeekEnd();
                }

                return true;
            }
        }

        return super.dispatchKeyEvent(pEvent);
    }

    @Override
    protected void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {
        //if (mBillingProcessor == null || !mBillingProcessor.handleActivityResult(pRequestCode, pResultCode, pData)) {
        super.onActivityResult(pRequestCode, pResultCode, pData);

        if (pRequestCode == REQUESTCODE_PERMISSIONS) {
            return;
        }

        // check if everything is ok
        if (pResultCode != RESULT_OK) {
            return;
        }

        if (pRequestCode == REQUESTCODE_CATEGORIES) {

            // !!! --> we dont get notified if a new category was created, because
            // this doesn't make any changed in the filters or existing categories

            // check if the filters were reset or
            // categories changed
            Bundle extras = pData.getExtras();
            boolean categoriesChanged = extras.getBoolean("categoriesChanged");
            boolean updateFilters = extras.getBoolean("updateFilters");

            if (categoriesChanged) {
                // the categories were changed, e.g. renamed
                mRecordingsFragment.loadRecordings(null);
            }
            if (updateFilters) {
                // show hint that the categoryfilters were reset
                new FilterDeletedDialog(this).show();
            }
        }
        //}
    }

    @Override
    public void onBackPressed() {
        Preferences.setIsProVersion(this, true);
        if (mBound && mRecordService != null && !mRecordService.getRecorder().isRecording()) {
            showCloseAppDialog();
        }
    }

    // PRIVATE API


    private boolean isInForeground() {
        switch (mActivityState) {
            case CREATED:
            case STARTED:
            case RESUMED:
                return true;
            case PAUSED:
            case STOPPED:
            case RESTARTED:
            case DESTROYED:
                return false;

        }
        return false;
    }

    /**
     * Unbinds the RecordService. May be used in the activity's lifecycle.
     */
    private void unbindService() {
        if (mBound) {
            unbindService(this);
            mBound = false;

            //disable recording-views, because we cannot access the service anymore
            setRecordingViewsEnabled(false);
        }
    }

    /**
     * Enables the recording-views
     *
     * @param pEnabled the new view-state
     */
    private void setRecordingViewsEnabled(boolean pEnabled) {
        mStartStopButton.setEnabled(pEnabled);
        mPauseResumeButton.setEnabled(pEnabled);
        mAbortButton.setEnabled(pEnabled);
    }

    /**
     * Hides the searchpanel. Beware of the TextWatcher: chose whether
     * to notify the textwatcher and trigger a new loading or not.
     *
     * @param pWithoutTextWatcher if the textwatcher should be notified
     */
    private void hideSearchPanel(boolean pWithoutTextWatcher) {
        if (mSearchRevealFrameLayout.getVisibility() == View.VISIBLE) {
            mSearchRevealFrameLayout.animateOut(Position.END, Position.BEGIN);
            if (mSearchEditText.length() != 0) {

                if (pWithoutTextWatcher) {
                    mSearchEditText.removeTextChangedListener(mSearchTextWatcher);
                }

                mSearchEditText.setText("");

                if (pWithoutTextWatcher) {
                    mSearchEditText.addTextChangedListener(mSearchTextWatcher);
                }

            }
            KeyboardHandler.hideKeyboard(mSearchEditText);
        }
    }

    /**
     * Shows the searchpanel.
     */
    private void showSearchPanel() {
        if (mSearchRevealFrameLayout.getVisibility() == GONE) {
            mSearchRevealFrameLayout.animateIn(Position.END, Position.BEGIN);
            KeyboardHandler.showKeyboard(mSearchEditText);
        }
    }

    private void showCloseAppDialog() {
        new ConfirmDialog(MainActivity.this, R.string.action_close, R.string.action_close_message, R.string.button_confirm_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface pDialog, int pWhich) {
                stopService(new Intent(MainActivity.this, RecordService.class));
                finish();
            }
        }).show();
    }

    // RECORDER CALLBACKS

    @Override
    public void onRecordStartIntent() {
        setRecordingViewsEnabled(false);
        mStartStopButton.setImageResource(R.drawable.recstop);
        mPauseResumeButton.setImageResource(R.drawable.pause_white);
        mPauseResumeButton.clearAnimation();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        hideSearchPanel(false);

        if (isKeepScreenOnWanted(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (mBound && mRecordService != null && mRecordService.getRecorder().isPausable()) {
            mRecordingViewsContainer.setLayoutTransition(null);
            mPauseResumeButton.setVisibility(View.VISIBLE);
            mRecordingViewsContainer.setLayoutTransition(new LayoutTransition());
        } else {
            mRecordingViewsContainer.setLayoutTransition(null);
            mPauseResumeButton.setVisibility(GONE);
            mRecordingViewsContainer.setLayoutTransition(new LayoutTransition());
        }
    }

    @Override
    public void onRecordStartSuccessful() {
        setRecordingViewsEnabled(true);
        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordService.getRecorder().getTotalTime());
        mChronometer.start();
        mRecordingScreenLayout.animateIn(Position.MID, Position.END);

        if (mBound && mRecordService != null && mRecordService.getRecorder().canDetectSilence() && isDetectSilenceWanted(this)) {
            mAmplitudeView.setSilenceGateAmplitude(getDetectSilenceGate(this));
            mAmplitudeView.setSilenceGateAmplitudeEnabled(true);
        }
    }

    @Override
    public void onRecordFail(Exception pException) {
        makeText(this, "Something went wrong. Recorder not started!", LENGTH_SHORT).show();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAmplitudeView.setSilenceGateAmplitudeEnabled(false);
        setRecordingViewsEnabled(true);
    }

    @Override
    public void onRecordStopIntent() {
        setRecordingViewsEnabled(false);
        mStartStopButton.setImageResource(R.drawable.recmic);
        mPauseResumeButton.setImageResource(R.drawable.pause_white);
        mPauseResumeButton.clearAnimation();
        mChronometer.stop();
        mRecordingScreenLayout.animateOut(Position.MID, Position.END);
        mSilenceIndicatorTextView.setVisibility(GONE);
        mSeekLimitReachedIndicatorTextView.setVisibility(GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onRecordStopSuccessful(String pFilePath) {
        //we are ready for the next recording!
        mStartStopButton.setEnabled(true);
        mPauseResumeButton.setEnabled(false);
        mAbortButton.setEnabled(false);
        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordService.getRecorder().getTotalTime());
        mAmplitudeView.setSilenceGateAmplitudeEnabled(false);

        if (isQuickRenamingWanted(this) && isInForeground()) {
            final Record record = DatabaseHelper.getInstance(this).getRecordById((int) RecordPreferences.getLastRecordId(this));
            RenameDialog dialog = new RenameDialog(this, record.getName(), new RenameDialog.OnRenameListener() {
                @Override
                public void onRenameClick(String pNewTitle) {
                    record.setName(pNewTitle);
                    DatabaseHelper.getInstance(MainActivity.this).updateRecord(record);
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mRecordingsFragment.loadRecordings(null);
                }
            });
            dialog.show();
        } else {
            //reload the list
            mRecordingsFragment.loadRecordings(null);
        }

    }

    @Override
    public void onRecordPauseIntent() {
        setRecordingViewsEnabled(false);
        mPauseResumeButton.setImageResource(R.drawable.resume_white);
        mPauseResumeButton.startAnimation(AnimationFactory.getFlashingAnimation());
        mChronometer.stop();
    }

    @Override
    public void onRecordResumeIntent() {
        setRecordingViewsEnabled(false);
        mPauseResumeButton.setImageResource(R.drawable.pause_white);
        mPauseResumeButton.clearAnimation();
    }

    @Override
    public void onRecordPaused() {
        setRecordingViewsEnabled(true);
    }

    @Override
    public void onRecordResumed() {
        setRecordingViewsEnabled(true);
        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordService.getRecorder().getTotalTime());
        mChronometer.start();
    }

    @Override
    public void onNewAmplitude(int pAmplitude) {
        mAmplitudeView.setAmplitudeAnimated(pAmplitude);
    }

    @Override
    public void onSilenceStart() {
        mSilenceIndicatorTextView.setVisibility(View.VISIBLE);
        mSilenceIndicatorTextView.startAnimation(AnimationFactory.getFlashingAnimation());
        mChronometer.stop();
    }

    @Override
    public void onSilenceEnd() {
        mSilenceIndicatorTextView.setVisibility(GONE);
        mSilenceIndicatorTextView.clearAnimation();
        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordService.getRecorder().getTotalTime());
        mChronometer.start();
    }

    @Override
    public void onSeek() {
        setRecordingViewsEnabled(false);
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordService.getRecorder().getTotalTime());
        if (mBound && mRecordService != null && mRecordService.getRecorder().isRecording()) {
            String currentSize = FormatUtils.toReadableSize(mRecordService.getRecorder().getCurrentSizeInByte());
            String freeSpace = FormatUtils.toReadableSize(StorageUtils.getFreeStorage(this)) + " " + getString(R.string.remaining);
            mRealTimeInfoTextView.setText(currentSize + " (" + freeSpace + ")");
        }
    }

    @Override
    public void onSeekEnd() {
        setRecordingViewsEnabled(true);
        mSeekLimitReachedIndicatorTextView.setVisibility(GONE);
        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordService.getRecorder().getTotalTime());
        if (!mRecordService.getRecorder().isPaused()) {
            mChronometer.start();
        }
    }

    @Override
    public void onSeekLimitReached() {
        mSeekLimitReachedIndicatorTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNewSize(long pSizeInByte) {
        String currentSize = FormatUtils.toReadableSize(mRecordService.getRecorder().getCurrentSizeInByte());
        String freeSpace = FormatUtils.toReadableSize(StorageUtils.getFreeStorage(this)) + " " + getString(R.string.remaining);
        mRealTimeInfoTextView.setText(currentSize + " (" + freeSpace + ")");
    }

    @Override
    public void onRecordAbortIntent() {
        setRecordingViewsEnabled(false);
        mStartStopButton.setImageResource(R.drawable.recmic);
        mPauseResumeButton.setImageResource(R.drawable.pause_white);
        mPauseResumeButton.clearAnimation();
        mChronometer.stop();
        mRecordingScreenLayout.animateOut(Position.MID, Position.END);
        mSilenceIndicatorTextView.setVisibility(GONE);
        mSeekLimitReachedIndicatorTextView.setVisibility(GONE);
        mSilenceIndicatorTextView.clearAnimation();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onRecordAbortSuccess() {
        mStartStopButton.setEnabled(true);
        mPauseResumeButton.setEnabled(false);
        mPauseResumeButton.clearAnimation();
        mAbortButton.setEnabled(false);
        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordService.getRecorder().getTotalTime());
        mAmplitudeView.setSilenceGateAmplitudeEnabled(false);
    }

    // SERVICE CONNECTION

    @Override
    public void onServiceConnected(ComponentName pName, IBinder pService) {
        mBound = true;
        ServiceBinder binder = (ServiceBinder) pService;
        mRecordService = binder.getService();
        mRecordService.getRecorder().setActivityOnRecordActionListener(this);

        //we got connection and can record, but maybe we do already
        mStartStopButton.setEnabled(true);

        mChronometer.setBase(SystemClock.elapsedRealtime() - mRecordService.getRecorder().getTotalTime());

        if (mRecordService.getRecorder().isPausable()) {
            mRecordingViewsContainer.setLayoutTransition(null);
            mPauseResumeButton.setVisibility(View.VISIBLE);
            mRecordingViewsContainer.setLayoutTransition(new LayoutTransition());
        } else {
            mRecordingViewsContainer.setLayoutTransition(null);
            mPauseResumeButton.setVisibility(GONE);
            mRecordingViewsContainer.setLayoutTransition(new LayoutTransition());
        }

        if (mRecordService.getRecorder().isRecording()) {
            // view visibilities
            mRecordingScreenLayout.setVisibility(View.VISIBLE);
            mStartStopButton.setImageResource(R.drawable.recstop);
            mPauseResumeButton.setEnabled(true);
            mAbortButton.setEnabled(true);

            if (mRecordService.getRecorder().canDetectSilence() && isDetectSilenceWanted(this)) {
                mAmplitudeView.setSilenceGateAmplitude(getDetectSilenceGate(this));
                mAmplitudeView.setSilenceGateAmplitudeEnabled(true);
            }

            // current recording size
            String currentSize = FormatUtils.toReadableSize(mRecordService.getRecorder().getCurrentSizeInByte());
            String freeSpace = FormatUtils.toReadableSize(StorageUtils.getFreeStorage(this)) + " " + getString(R.string.remaining);
            mRealTimeInfoTextView.setText(currentSize + " (" + freeSpace + ")");

            // state recovery
            if (mRecordService.getRecorder().isPaused()) {
                mPauseResumeButton.setImageResource(R.drawable.resume_white);
                mPauseResumeButton.startAnimation(AnimationFactory.getFlashingAnimation());
            } else {
                mPauseResumeButton.setImageResource(R.drawable.pause_white);

                if (mRecordService.getRecorder().isInSilence()) {
                    onSilenceStart();
                } else {
                    mChronometer.start();
                }
            }
        } else {
            mStartStopButton.setImageResource(R.drawable.recmic);
            mPauseResumeButton.setEnabled(false);
            mAbortButton.setEnabled(false);
            mPauseResumeButton.setImageResource(R.drawable.pause_white);
            mPauseResumeButton.clearAnimation();
            mAmplitudeView.setSilenceGateAmplitudeEnabled(false);
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName pName) {
        mBound = false;

        //as we are not connected, disable the record-button
        mStartStopButton.setEnabled(false);
        mPauseResumeButton.setEnabled(false);
        mAbortButton.setEnabled(false);
    }

    // RECORDINGS FRAGMENT CALLBACKS

    @Override
    public void onRecordingsChanged(boolean pFiltered) {
        // called when the loading was finished.
        // pFiltered if the data changed because of a filtering
        // !pFiltered if the data changed because of renaming etc.
        if (!pFiltered) {
            hideSearchPanel(true);
        }
    }

    @Override
    public void onNoResultsResetButtonClick() {
        mSearchEditText.setText("");
    }

    // VIEW CLICKS

    @OnClick(R.id.recordingsAbortButton)
    protected void onAbortButtonClick() {
        if (isConfirmAbortWanted(this)) {
            new ConfirmDialog(this, R.string.abort_recording, R.string.abort_recording_message, R.string.button_abort, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface pDialog, int pWhich) {
                    if (mBound && mRecordService != null && mRecordService.getRecorder().isRecording()) {
                        mRecordService.getRecorder().abort();
                    }
                    pDialog.dismiss();
                }
            }).show();
        } else if (mBound && mRecordService != null && mRecordService.getRecorder().isRecording()) {
            mRecordService.getRecorder().abort();
        }
    }

    @OnClick(R.id.recordingsPauseResumeButton)
    protected void onPauseResumeButtonClick() {
        if (mBound && mRecordService != null && mRecordService.getRecorder().isRecording()) {
            if (mRecordService.getRecorder().isPaused()) {
                mRecordService.getRecorder().resume();
            } else {
                mRecordService.getRecorder().pause();
            }
        }
    }

    @OnClick(R.id.recordingsStartStopButton)
    protected void onStartStopButtonClick() {
        //if we are connected to the service
        if (mBound && mRecordService != null) {
            if (mRecordService.getRecorder().isRecording()) {
                // stop recording
                mRecordService.getRecorder().stop();
            } else {
                mRecordService.getRecorder().start();
            }
        }
    }

    @OnClick(R.id.categoriesButton)
    protected void onCategoriesIconClick() {
        startActivityForResult(new Intent(this, CategoriesActivity.class), REQUESTCODE_CATEGORIES);
    }

    @OnClick(R.id.quickSettingsButton)
    protected void onQuickSettingsButtonClick() {
        new QuickSettingsDialog(this).show();
    }

    @OnClick(R.id.menuButton)
    protected void onMenuButtonClick() {
        PopupMenu menu = new PopupMenu(new ContextThemeWrapper(this, R.style.PopupStyle), findViewById(R.id.menuButton));
        menu.inflate(R.menu.menu_main);
        menu.show();

        Sorting sorting = Preferences.getRecordingsSorting(this);

        switch (sorting) {
            case NAME_ASC:
                menu.getMenu().findItem(R.id.action_sort_group_name_asc).setChecked(true);
                break;
            case NAME_DESC:
                menu.getMenu().findItem(R.id.action_sort_group_name_desc).setChecked(true);
                break;
            case CREATED_ASC:
                menu.getMenu().findItem(R.id.action_sort_group_time_asc).setChecked(true);
                break;
            case CREATED_DESC:
                menu.getMenu().findItem(R.id.action_sort_group_time_desc).setChecked(true);
                break;
        }


        menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem pItem) {
                switch (pItem.getItemId()) {
                    case R.id.action_search:
                        showSearchPanel();
                        break;
                    case R.id.action_filter:
                        new FilterDialog(MainActivity.this, new FilterDialog.OnApplyClickListener() {
                            @Override
                            public void onApplyClick() {
                                mRecordingsFragment.loadRecordings(null);
                            }
                        }).show();
                        break;
                    case R.id.action_wifitransfer:
                        new WifiTransferDialog(MainActivity.this).show();
                        break;
                    case R.id.action_settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                    case R.id.action_close:
                        showCloseAppDialog();
                        break;
                    case R.id.action_sort_group_name_asc:
                        Preferences.setRecordingsSorting(MainActivity.this, Sorting.NAME_ASC);
                        mRecordingsFragment.loadRecordings(null);
                        break;
                    case R.id.action_sort_group_name_desc:
                        Preferences.setRecordingsSorting(MainActivity.this, Sorting.NAME_DESC);
                        mRecordingsFragment.loadRecordings(null);
                        break;
                    case R.id.action_sort_group_time_asc:
                        Preferences.setRecordingsSorting(MainActivity.this, Sorting.CREATED_ASC);
                        mRecordingsFragment.loadRecordings(null);
                        break;
                    case R.id.action_sort_group_time_desc:
                        Preferences.setRecordingsSorting(MainActivity.this, Sorting.CREATED_DESC);
                        mRecordingsFragment.loadRecordings(null);
                        break;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.searchCloseButton)
    protected void onSearchCloseButtonClick() {
        hideSearchPanel(false);
    }

    @OnClick(R.id.premiumButton)
    protected void onPremiumButtonClick() {

        mPremiumButton.setVisibility(GONE);
        findViewById(R.id.spinner).setVisibility(View.VISIBLE);

        List<String> skuList = new ArrayList<>();
        skuList.add(Constants.PREMIUM_SKU);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        // Process the result.
                        if(responseCode == 0 && skuDetailsList.size() != 0) {
                            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetailsList.get(0))
                                    .build();
                            mBillingClient.launchBillingFlow(MainActivity.this, flowParams);
                        }

                        if(skuDetailsList.isEmpty()) {
                            throw new RuntimeException("onSkuDetailsResponse with responseCode " + responseCode +", Billingclient was " + (mBillingClient.isReady() ? "ready" : "not ready") +", skuDetailsList size is " + skuDetailsList.size());
                        }
                    }
                });
    }

    // IN APP PURCHASES

    @Override
    public void onBillingSetupFinished(int responseCode) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            // The billing client is ready. You can query purchases here.
            mPremiumButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            Preferences.setIsProVersion(this, true);
            mPremiumButton.setVisibility(GONE);
            findViewById(R.id.spinner).setVisibility(GONE);
            findViewById(R.id.freeversionMessageView).setVisibility(GONE);
            new OkDialog(this, R.string.title_purchased, R.string.message_purchased, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface pDialog, int pWhich) {
                    pDialog.dismiss();
                }
            }).show();
        } else if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
            Preferences.setIsProVersion(this, true);
            mPremiumButton.setVisibility(GONE);
            findViewById(R.id.spinner).setVisibility(GONE);
            findViewById(R.id.freeversionMessageView).setVisibility(GONE);
            new OkDialog(this, R.string.title_purchased_already, R.string.message_purchased_already, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface pDialog, int pWhich) {
                    pDialog.dismiss();
                }
            }).show();
        } else {
            findViewById(R.id.spinner).setVisibility(GONE);
            mPremiumButton.setVisibility(View.VISIBLE);
        }
    }


}
