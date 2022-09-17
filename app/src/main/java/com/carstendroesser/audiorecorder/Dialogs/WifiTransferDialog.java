package com.carstendroesser.audiorecorder.Dialogs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.carstendroesser.audiorecorder.R;
import com.carstendroesser.audiorecorder.server.WifiTransferServer;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carstendrosser on 22.11.15.
 */
public class WifiTransferDialog extends AlertDialog implements DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    @Bind(R.id.buttonCancel)
    protected Button mButtonCancel;
    @Bind(R.id.errorContainer)
    protected View mErrorContainer;
    @Bind(R.id.content)
    protected View mContent;
    @Bind(R.id.ipAdressTextView)
    protected TextView mIpAdress;

    private WifiTransferServer mServer;
    private BroadcastReceiver mWifiObserver;

    private static final int PORT = 8080;

    public WifiTransferDialog(Context pContext) {
        super(pContext);

        getWindow().getAttributes().windowAnimations = R.style.DialogsAnimation;

        // inflate view
        View content = LayoutInflater.from(pContext).inflate(R.layout.dialog_wifitransfer, null);
        setView(content);

        ButterKnife.bind(this, content);

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                stopServer();
                dismiss();
            }
        });

        setOnShowListener(this);
        setOnDismissListener(this);

        setCancelable(false);

        mWifiObserver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context pContext, Intent pIntent) {
                WifiManager wifiMgr = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiMgr.isWifiEnabled()) {
                    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                    if (wifiInfo.getNetworkId() == -1) {
                        stopServer();
                        mContent.setVisibility(View.GONE);
                        mErrorContainer.setVisibility(View.VISIBLE);
                    } else {
                        try {
                            startServer();
                        } catch (IOException e) {
                            e.printStackTrace();
                            // TODO ERROR HANDLING
                        }
                        mContent.setVisibility(View.VISIBLE);
                        mErrorContainer.setVisibility(View.GONE);
                    }
                } else {
                    stopServer();
                    mContent.setVisibility(View.GONE);
                    mErrorContainer.setVisibility(View.VISIBLE);
                }
            }
        };

        IntentFilter filters = new IntentFilter();
        filters.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filters.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        pContext.registerReceiver(mWifiObserver, filters);
    }


    @Override
    public void onShow(DialogInterface pDialog) {
        WifiManager wifiMgr = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        boolean wifiConnected = false;

        if (wifiMgr.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo.getNetworkId() == -1) {
                wifiConnected = false;
            } else {
                wifiConnected = true;
            }
        } else {
            wifiConnected = false;
        }

        if (wifiConnected) {
            try {
                startServer();
            } catch (IOException pException) {
                pException.printStackTrace();
                //TODO ERROR HANDLING
            }
            mContent.setVisibility(View.VISIBLE);
            mErrorContainer.setVisibility(View.GONE);
        } else {
            mContent.setVisibility(View.GONE);
            mErrorContainer.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        stopServer();
        getContext().unregisterReceiver(mWifiObserver);
    }

    private void startServer() throws IOException {
        if (mServer != null) {
            mServer.closeAllConnections();
            mServer.stop();
        }
        mServer = new WifiTransferServer(getContext(), PORT);
        mServer.start();

        int ipAddress = ((WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        mIpAdress.setText(formatedIpAddress + ":" + PORT);
    }

    private void stopServer() {
        if (mServer != null) {
            mServer.closeAllConnections();
            mServer.stop();
        }
    }

}
