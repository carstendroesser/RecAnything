package com.carstendroesser.audiorecorder.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.fragments.NestedSettingsFragment;
import com.carstendroesser.audiorecorder.fragments.SettingsFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 14.10.17.
 */

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.Callback {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private static final String TAG_NESTED = "TAG_NESTED";
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        //use toolbar as actionbar
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                onBackPressed();
            }
        });

        if (pSavedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.contentSettings, mSettingsFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onNestedPreferenceSelected(int pKey) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.contentSettings, NestedSettingsFragment.newInstance(pKey), TAG_NESTED)
                .addToBackStack(TAG_NESTED)
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSettingsFragment.isVisible()) {
            mSettingsFragment.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
