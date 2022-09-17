package com.carstendroesser.audiorecorder.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.carstendroesser.audiorecorder.Dialogs.InformationDialog;
import com.carstendroesser.audiorecorder.Dialogs.OkDialog;
import com.carstendroesser.audiorecorder.Dialogs.PrivacyDialog;
import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.activities.MainActivity;
import com.carstendroesser.audiorecorder.utils.Constants;
import com.carstendroesser.audiorecorder.utils.IntentFactory;
import com.carstendroesser.audiorecorder.utils.Preferences;
import com.carstendroesser.audiorecorder.views.PreferenceBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by carstendrosser on 14.10.17.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, BillingClientStateListener, PurchasesUpdatedListener {

    private Callback mCallback;

    private static final String KEY_GENERAL = "NESTED_KEY_GENERAL";
    private static final String KEY_RECORDER = "NESTED_KEY_RECORDER";
    private static final String KEY_UI = "NESTED_KEY_UI";
    private static final String KEY_CLOUD = "NESTED_KEY_CLOUD";
    private static final String KEY_CONTACT = "CONTACT";
    private static final String KEY_ABOUT = "ABOUT";
    private static final String KEY_PRIVACY_POLICY = "PRIVACY_POLICY";
    public static final String KEY_PREMIUM_PREFERENCE = "PREMIUM_PREFERENCE";

    private PreferenceBlock mPremiumPreference;

    private BillingClient mBillingClient;

    @Override
    public void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        if (getActivity() instanceof Callback) {
            mCallback = (Callback) getActivity();
        } else {
            throw new IllegalStateException("Owner must implement Callback interface");
        }

        addPreferencesFromResource(R.xml.preferences_main);

        Preference preference = findPreference(KEY_GENERAL);
        preference.setOnPreferenceClickListener(this);
        preference = findPreference(KEY_RECORDER);
        preference.setOnPreferenceClickListener(this);
        preference = findPreference(KEY_UI);
        preference.setOnPreferenceClickListener(this);
        preference = findPreference(KEY_CLOUD);
        preference.setOnPreferenceClickListener(this);
        preference = findPreference(KEY_CONTACT);
        preference.setOnPreferenceClickListener(this);
        preference = findPreference(KEY_ABOUT);
        preference.setOnPreferenceClickListener(this);
        preference = findPreference(KEY_PRIVACY_POLICY);
        preference.setOnPreferenceClickListener(this);

        mPremiumPreference = (PreferenceBlock) findPreference(KEY_PREMIUM_PREFERENCE);
        mPremiumPreference.setOnButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                mPremiumPreference.setView(PreferenceBlock.WhichView.SPINNER);
                List<String> skuList = new ArrayList<>();
                skuList.add(Constants.PREMIUM_SKU);
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                mBillingClient.querySkuDetailsAsync(params.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                                // Process the result.
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0))
                                        .build();
                                mBillingClient.launchBillingFlow(getActivity(), flowParams);
                            }
                        });
            }
        });

        if (Preferences.getIsProVersion(getActivity())) {
            getPreferenceScreen().removePreference(mPremiumPreference);
        } else {
            mBillingClient = BillingClient.newBuilder(getActivity()).setListener(this).build();
            mBillingClient.startConnection(this);
        }

    }

    @Override
    public void onViewCreated(View pView, @Nullable Bundle pSavedInstanceState) {
        super.onViewCreated(pView, pSavedInstanceState);
        ListView list = pView.findViewById(android.R.id.list);
        list.setDivider(null);
        list.setPadding(0, 0, 0, 0);
    }

    @Override
    public boolean onPreferenceClick(Preference pPreference) {
        if (pPreference.getKey().equals(KEY_GENERAL)) {
            mCallback.onNestedPreferenceSelected(NestedSettingsFragment.NESTED_SCREEN_GENERAL);
        } else if (pPreference.getKey().equals(KEY_RECORDER)) {
            mCallback.onNestedPreferenceSelected(NestedSettingsFragment.NESTED_SCREEN_RECORDER);
        } else if (pPreference.getKey().equals(KEY_UI)) {
            mCallback.onNestedPreferenceSelected(NestedSettingsFragment.NESTED_SCREEN_UI);
        } else if (pPreference.getKey().equals(KEY_CLOUD)) {
            mCallback.onNestedPreferenceSelected(NestedSettingsFragment.NESTED_SCREEN_CLOUD);
        } else if (pPreference.getKey().equals(KEY_CONTACT)) {
            startActivity(Intent.createChooser(IntentFactory.getContactIntent(getActivity().getApplicationContext()), "Select:"));
        } else if (pPreference.getKey().equals(KEY_ABOUT)) {
            showAboutDialog();
        } else if (pPreference.getKey().equals(KEY_PRIVACY_POLICY)) {
            showPrivacyDialog();
        }

        return false;
    }

    private void showAboutDialog() {
        new InformationDialog(getActivity()).show();
    }

    private void showPrivacyDialog() {
        new PrivacyDialog(getActivity()).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.settings_title));
    }

    @Override
    public void onBillingSetupFinished(int responseCode) {

    }

    @Override
    public void onBillingServiceDisconnected() {

    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        mPremiumPreference.setView(PreferenceBlock.WhichView.BUTTON);

        if(responseCode == BillingClient.BillingResponse.OK) {
            Preferences.setIsProVersion(getActivity(), true);
            getPreferenceScreen().removePreference(mPremiumPreference);
            new OkDialog(getActivity(), R.string.title_purchased, R.string.message_purchased, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface pDialog, int pWhich) {
                    pDialog.dismiss();
                }
            }).show();
        } else if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
            Preferences.setIsProVersion(getActivity(), true);
            getPreferenceScreen().removePreference(mPremiumPreference);
            new OkDialog(getActivity(), R.string.title_purchased_already, R.string.message_purchased_already, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface pDialog, int pWhich) {
                    pDialog.dismiss();
                }
            }).show();
        }
    }

    public interface Callback {
        void onNestedPreferenceSelected(int pKey);
    }

}
