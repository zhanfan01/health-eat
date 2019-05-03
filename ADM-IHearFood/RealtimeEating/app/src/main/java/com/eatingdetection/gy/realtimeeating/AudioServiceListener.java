package com.eatingdetection.gy.realtimeeating;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by GY on 3/19/2016.
 */
public class AudioServiceListener extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        // Start AudioService when get a broadcast
        context.startService(new Intent(context, AudioService.class));
    }
}
