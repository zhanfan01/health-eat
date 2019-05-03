package com.eatingdetection.gy.ihearfood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.eatingdetection.gy.ihearfood.FoodLog.T2Sservice;

/**
 * Created by GY on 3/19/2016.
 */
public class AudioServiceListener extends BroadcastReceiver {
    private static final String TAG = "AudioServiceListener";
    private boolean block = false;
    //Intent noNetworkIntent = new Intent("No_Network");

    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo WIFIinfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiAvail = WIFIinfo.isAvailable();
        boolean isWifiConn = WIFIinfo.isConnected();
        NetworkInfo cellularinfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileAvail = cellularinfo.isAvailable();
        boolean isMobilConn = cellularinfo.isConnected();

        if (intent.hasExtra("Block")) {
            Log.d(TAG,"Set Block");
            block = intent.getExtras().getBoolean("Block");
        } else {
            if (block) {
                Log.d(TAG, "AudioService is running. Intent is blocked.");
            } else {
                if (isWifiConn || isMobilConn) {
                    // Start AudioService when get a broadcast
                    Log.d(TAG, "Try to start audio service!");
                    context.startService(new Intent(context, AudioService.class));
                } else {
                    Log.d(TAG, "No Internet");
                    //noNetworkIntent.putExtra("noInternet", "Please connect you phone to Internet");
                    //context.sendBroadcast(noNetworkIntent);
                    T2Sservice.mNotification("Please connect your phone to Internet");
                }
            }
        }
    }
}
