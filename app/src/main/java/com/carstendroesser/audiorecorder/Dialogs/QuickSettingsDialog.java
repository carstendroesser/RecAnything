package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.activities.SettingsActivity;
import com.carstendroesser.audiorecorder.events.RecorderChangedEvent;
import com.carstendroesser.audiorecorder.utils.GainvalueUtils;
import com.carstendroesser.audiorecorder.utils.Settings;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 20.11.15.
 */
public class QuickSettingsDialog extends AlertDialog {

    @Bind(R.id.buttonClose)
    protected Button mButtonClose;
    @Bind(R.id.recorderSpinner)
    protected Spinner mRecorderSpinner;
    @Bind(R.id.skipSilenceSwitch)
    protected SwitchCompat mSkipSilenceSwitch;
    @Bind(R.id.gainvalueTitleTextView)
    protected TextView mGainValueTextView;
    @Bind(R.id.gainvalueSeekBar)
    protected SeekBar mGainValueSeekBar;
    @Bind(R.id.quickSettingsAttentionTextView)
    protected TextView mQuickSettingsAttentionTextView;

    public QuickSettingsDialog(final Context pContext) {
        super(pContext);

        // set style
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_quicksettings, null);
        setView(content);

        ButterKnife.bind(this, content);

        mButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                dismiss();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(pContext,
                R.array.recorderEntries, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRecorderSpinner.setAdapter(adapter);
        mRecorderSpinner.setSelection(Settings.getChoosenRecorder(pContext), false);
        mRecorderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> pParent, View pView, int pPosition, long pId) {
                Settings.setChoosenRecorder(pContext, pPosition);
                EventBus.getDefault().post(new RecorderChangedEvent(pPosition));
                updateViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSkipSilenceSwitch.setChecked(Settings.isDetectSilenceWanted(getContext()));
        mGainValueSeekBar.setProgress(GainvalueUtils.getSelectedGainValueIndex(getContext()));
        mGainValueTextView.setText(getContext().getString(R.string.settings_title_gainvalue) + " (" + (GainvalueUtils.getSelectedGainValueIndex(getContext()) * 2) + " dB)");


        mSkipSilenceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton pButtonView, boolean pIsChecked) {
                Settings.setDetectSilenceWanted(pContext, pIsChecked);
            }
        });

        mGainValueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar pSeekBar, int pProgress, boolean pFromUser) {
                if (pFromUser) {
                    mGainValueTextView.setText(getContext().getString(R.string.settings_title_gainvalue) + " (" + (pProgress * 2) + " dB)");
                    Settings.setGainValue(getContext(), GainvalueUtils.dbToGainValue(getContext(), pProgress * 2));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar pSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar pSeekBar) {

            }
        });

        String nonClickablePart = pContext.getString(R.string.quicksettings_attention);
        String clickablePart = pContext.getString(R.string.go_to_all_settings);

        SpannableString text = new SpannableString(nonClickablePart + " " + clickablePart);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                dismiss();
                pContext.startActivity(new Intent(pContext, SettingsActivity.class));
            }
        };

        text.setSpan(clickableSpan, text.length() - clickablePart.length(), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mQuickSettingsAttentionTextView.setText(text);
        mQuickSettingsAttentionTextView.setMovementMethod(LinkMovementMethod.getInstance());
        mQuickSettingsAttentionTextView.setHighlightColor(Color.TRANSPARENT);

        updateViews();
    }

    private void updateViews() {
        if (Settings.getChoosenRecorder(getContext()) == 0 || Settings.getChoosenRecorder(getContext()) == 1) {
            mSkipSilenceSwitch.setEnabled(true);
            mGainValueSeekBar.setEnabled(true);
        } else {
            mSkipSilenceSwitch.setEnabled(false);
            mGainValueSeekBar.setEnabled(false);
        }
    }

}
