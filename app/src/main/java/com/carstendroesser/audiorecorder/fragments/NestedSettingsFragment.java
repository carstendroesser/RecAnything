package com.carstendroesser.audiorecorder.fragments;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import com.carstendroesser.audiorecorder.Dialogs.ConfirmDialog;
import com.carstendroesser.audiorecorder.Dialogs.DirectoryPickerDialog;
import com.carstendroesser.audiorecorder.Dialogs.MessageDialog;
import com.carstendroesser.audiorecorder.Dialogs.OkDialog;
import com.carstendroesser.audiorecorder.Dialogs.ProgressDialog;
import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.cloud.DropboxHelper;
import com.carstendroesser.audiorecorder.events.RecorderChangedEvent;
import com.carstendroesser.audiorecorder.utils.IntentFactory;
import com.carstendroesser.audiorecorder.utils.PermissionUtils;
import com.carstendroesser.audiorecorder.utils.PermissionsChecker;
import com.carstendroesser.audiorecorder.utils.Preferences;
import com.carstendroesser.audiorecorder.utils.Settings;
import com.dropbox.core.DbxException;
import com.dropbox.core.android.Auth;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * Created by carstendrosser on 14.10.17.
 */

public class NestedSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    public static final int NESTED_SCREEN_GENERAL = 0;
    public static final int NESTED_SCREEN_RECORDER = 1;
    public static final int NESTED_SCREEN_UI = 2;
    public static final int NESTED_SCREEN_CLOUD = 3;

    private static final String KEY_PATH = "PATH";
    private static final String KEY_RECORDER = "recorder";
    private static final String KEY_ACCOUNTDROPBOX = "ACCOUNT_DROPBOX";
    private static final String KEY_AUTOUPLOAD = "cloudautomaticupload";
    private static final String KEY_DO_NOT_DISTURB = "silentringermode";

    private static final String TAG_KEY = "NESTED_KEY";
    private static final String DROPBOX_APPKEY = "iva99lxx7z2rvoe";

    ProgressDialog mProgressDialog;

    public static NestedSettingsFragment newInstance(int pKey) {
        NestedSettingsFragment fragment = new NestedSettingsFragment();
        Bundle args = new Bundle();
        args.putInt(TAG_KEY, pKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        checkPreferenceResource();
    }

    @Override
    public void onViewCreated(View pView, @Nullable Bundle pSavedInstanceState) {
        super.onViewCreated(pView, pSavedInstanceState);
        ListView list = (ListView) pView.findViewById(android.R.id.list);
        list.setDivider(null);
        list.setPadding(0, 0, 0, 0);
    }

    private void checkPreferenceResource() {
        int key = getArguments().getInt(TAG_KEY);

        final Preference preference;

        switch (key) {
            case NESTED_SCREEN_GENERAL:
                addPreferencesFromResource(R.xml.preferences_general);
                getActivity().setTitle(getString(R.string.settings_sub_general));

                preference = findPreference(KEY_PATH);
                preference.setOnPreferenceClickListener(this);

                final Preference doNotDisturbPreference = findPreference(KEY_DO_NOT_DISTURB);

                doNotDisturbPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference pPreference, Object pNewValue) {
                        boolean shallBeActivated = (boolean) pNewValue;

                        if(shallBeActivated) {
                            if(!PermissionsChecker.hasDoNotDisturbPermission(pPreference.getContext())){
                                new ConfirmDialog(doNotDisturbPreference.getContext(), R.string.permission_silent_ringer_title, R.string.permission_silent_ringer_message, R.string.permission_silent_ringer_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface pDialog, int pWhich) {
                                        // go to the settings
                                        startActivity(IntentFactory.getDoNotDisturbPermissionIntent());
                                        pDialog.dismiss();
                                    }
                                }).show();
                                return false;
                            } else {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    }
                });

                break;
            case NESTED_SCREEN_RECORDER:
                addPreferencesFromResource(R.xml.preferences_recorder);
                getActivity().setTitle(getString(R.string.settings_sub_recorder));

                setRecordingPreferencesFor(Settings.getChoosenRecorder(getActivity().getApplicationContext()));

                preference = findPreference(KEY_RECORDER);
                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(final Preference pPreference, final Object pNewValue) {
                        // 0 = wav, 1 = mp3, 2 = aac, 3 = 3gp
                        final int val = Integer.parseInt(pNewValue.toString());

                        if (val == Settings.getChoosenRecorder(pPreference.getContext())) {
                            return true;
                        }

                        if (val == 0) {
                            setRecordingPreferencesFor(0);
                            EventBus.getDefault().post(new RecorderChangedEvent(val));
                            return true;
                        } else if (val == 1) {
                            new ConfirmDialog(pPreference.getContext(), R.string.confirm_recorder_change, R.string.confirm_recorder_change_text_mp3, R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface pDialog, int pWhich) {
                                    pDialog.dismiss();
                                    setRecordingPreferencesFor(1);
                                    Settings.setChoosenRecorder(pPreference.getContext(), 1);
                                    ((ListPreference) preference).setValue("" + 1);
                                    EventBus.getDefault().post(new RecorderChangedEvent(1));
                                }
                            }).show();
                        } else if (val == 2) {
                            new ConfirmDialog(pPreference.getContext(), R.string.confirm_recorder_change, R.string.confirm_recorder_change_text_aac, R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface pDialog, int pWhich) {
                                    pDialog.dismiss();
                                    setRecordingPreferencesFor(2);
                                    Settings.setChoosenRecorder(pPreference.getContext(), 2);
                                    ((ListPreference) preference).setValue("" + 2);
                                    EventBus.getDefault().post(new RecorderChangedEvent(2));
                                }
                            }).show();
                        } else if (val == 3) {
                            new ConfirmDialog(pPreference.getContext(), R.string.confirm_recorder_change, R.string.confirm_recorder_change_text_3gp, R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface pDialog, int pWhich) {
                                    pDialog.dismiss();
                                    setRecordingPreferencesFor(3);
                                    Settings.setChoosenRecorder(pPreference.getContext(), 3);
                                    ((ListPreference) preference).setValue("" + 3);
                                    EventBus.getDefault().post(new RecorderChangedEvent(3));
                                }
                            }).show();
                        }

                        return false;
                    }
                });
                break;
            case NESTED_SCREEN_UI:
                addPreferencesFromResource(R.xml.preferences_ui);
                getActivity().setTitle(getString(R.string.settings_sub_interface));
                break;
            case NESTED_SCREEN_CLOUD:
                addPreferencesFromResource(R.xml.preferences_cloud);

                preference = findPreference(KEY_ACCOUNTDROPBOX);
                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference pPreference) {
                        Auth.startOAuth2Authentication(getActivity(), DROPBOX_APPKEY);
                        return false;
                    }
                });

                getActivity().setTitle(getString(R.string.settings_sub_cloud));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_PATH:
                showPathDialog();
                break;
        }
        return false;
    }

    private void showPathDialog() {
        DirectoryPickerDialog dialog = new DirectoryPickerDialog(getActivity(), new DirectoryPickerDialog.OnDirectorySelectedListener() {
            @Override
            public void onDirectorySelected(File pDirectory) {
                Settings.setRecordingPath(getActivity().getApplicationContext(), pDirectory.getPath());
            }
        });

        //do some stuff with your dialog and builder
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void setRecordingPreferencesFor(int pRecorder) {
        Preference preference;

        // wav & mp3 only
        preference = findPreference("samplerate");
        if (pRecorder == 0 || pRecorder == 1) {
            preference.setEnabled(true);
        } else {
            preference.setEnabled(false);
        }

        // wav & mp3
        preference = findPreference("echocanceling");
        if (pRecorder == 0 || pRecorder == 1) {
            preference.setEnabled(true);
        } else {
            preference.setEnabled(false);
        }

        // wav & mp3
        preference = findPreference("noisecanceling");
        if (pRecorder == 0 || pRecorder == 1) {
            preference.setEnabled(true);
        } else {
            preference.setEnabled(false);
        }

        // wav & mp3
        preference = findPreference("autogain");
        if (pRecorder == 0 || pRecorder == 1) {
            preference.setEnabled(true);
        } else {
            preference.setEnabled(false);
        }

        //wav & mp3
        preference = findPreference("gainvalue");
        if (pRecorder == 0 || pRecorder == 1) {
            preference.setEnabled(true);
        } else {
            preference.setEnabled(false);
        }

        // wav
        preference = findPreference("seeking");
        if (pRecorder == 0) {
            preference.setEnabled(true);
        } else {
            preference.setEnabled(false);
        }

        // wav & mp3
        preference = findPreference("detectsilence");
        if (pRecorder == 0 || pRecorder == 1) {
            preference.setEnabled(true);
        } else {
            preference.setEnabled(false);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        int key = getArguments().getInt(TAG_KEY);

        if (key == NESTED_SCREEN_CLOUD) {
            String accessToken = Preferences.getDropboxToken(getActivity());
            if (accessToken == null) {
                accessToken = Auth.getOAuth2Token();
                if (accessToken != null) {
                    Preferences.setDropboxToken(getActivity(), accessToken);
                    initDB(accessToken);
                }
            } else {
                initDB(accessToken);
            }
        }
    }

    protected void initDB(final String pAccessToken) {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity(), getString(R.string.looking_for_clouds));
        }

        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }

        DropboxHelper.init(getActivity(), pAccessToken);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String userEmail = "";
                try {
                    userEmail = DropboxHelper.getClient().users().getCurrentAccount().getEmail();
                } catch (DbxException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            new MessageDialog(getActivity(), R.string.cloud_error_title, R.string.cloud_error_message, R.string.no, R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface pDialog, int pWhich) {
                                    pDialog.dismiss();
                                    getActivity().onBackPressed();
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface pDialog, int pWhich) {
                                    pDialog.dismiss();
                                    initDB(pAccessToken);
                                }
                            }).show();
                        }
                    });
                }

                final String finalUserEmail = userEmail;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        findPreference(KEY_ACCOUNTDROPBOX).setTitle("Dropbox");
                        findPreference(KEY_ACCOUNTDROPBOX).setSummary(getString(R.string.connected_to) + " " + finalUserEmail);
                        findPreference(KEY_ACCOUNTDROPBOX).setOnPreferenceClickListener(null);
                        mProgressDialog.dismiss();
                        findPreference(KEY_AUTOUPLOAD).setEnabled(true);

                        findPreference(KEY_ACCOUNTDROPBOX).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                new MessageDialog(getActivity(), R.string.unconnect_dropbox_title, R.string.unconnect_dropbox_message, R.string.button_cancel, R.string.button_unconnect, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface pDialog, int pWhich) {
                                        pDialog.dismiss();
                                    }
                                }, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface pDialog, int pWhich) {
                                        pDialog.dismiss();
                                        final ProgressDialog dialog = new ProgressDialog(getActivity(), getActivity().getString(R.string.disconnecting_dropbox));
                                        dialog.show();
                                        DropboxHelper.reset(getActivity().getApplicationContext(), new DropboxHelper.OnResetDoneCallback() {
                                            @Override
                                            public void onResetSuccess() {
                                                findPreference(KEY_AUTOUPLOAD).setEnabled(false);
                                                ((SwitchPreference) findPreference(KEY_AUTOUPLOAD)).setChecked(false);
                                                Settings.setAutouploadWanted(getActivity(), false);

                                                Preference preference = findPreference(KEY_ACCOUNTDROPBOX);
                                                preference.setSummary(R.string.account_dropbox_summary);
                                                preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                                                    @Override
                                                    public boolean onPreferenceClick(Preference pPreference) {
                                                        Auth.startOAuth2Authentication(getActivity(), DROPBOX_APPKEY);
                                                        return false;
                                                    }
                                                });

                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onResetError() {
                                                dialog.dismiss();
                                                new OkDialog(getActivity(), R.string.unconnect_dropbox_error_title, R.string.unconnect_dropbox_error_message, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface pDialog, int pWhich) {
                                                        getActivity().onBackPressed();
                                                        pDialog.dismiss();
                                                    }
                                                }).show();
                                            }
                                        });
                                    }
                                }).show();
                                return false;
                            }
                        });

                    }
                });

            }
        }).start();

    }

}
