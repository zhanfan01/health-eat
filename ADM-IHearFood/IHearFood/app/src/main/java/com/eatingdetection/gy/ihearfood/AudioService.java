package com.eatingdetection.gy.ihearfood;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

/**
 * Created by GY on 3/19/2016.
 */
public class AudioService extends Service {
    private static final String TAG = "mEatingAudioService";
    protected static final String BLOCK_START = "com.eatingdetection.gy.ihearfood.Start_Record";

    private AudioManager mAudioManager = null;
    private boolean BluetoothScoIsStarted = false;

    Context context;

    Intent blockStart = new Intent(BLOCK_START);
    BroadcastReceiver stopSelfReceiver;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {

        context = getApplicationContext();

        Log.d(TAG, "Audio service started!");

        blockStart.putExtra("Block", true);
        sendBroadcast(blockStart);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if(!mAudioManager.isBluetoothScoAvailableOffCall()){
            Log.d(TAG, "No Bluetooth");
            Toast.makeText(this, "No Bluetooth Available", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "starting bluetooth");
            mAudioManager.startBluetoothSco();
            BluetoothScoIsStarted = true;
        }

        System.out.println("Listening Phone Call State!");
        //获得电话管理器
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //为管理器设置监听器,监听电话的呼叫状态
        manager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);

        //Start Audio Recording
        AudioRecordController.startRecording(context, BluetoothScoIsStarted);

        //Stop self listener for No_More_Eating
        registerReceiver(stopSelfReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                AudioRecordController.stopRecording(context, BluetoothScoIsStarted);
                removeBlock();

                Log.d(TAG, "Get Stop Intent! Stop Service now!");
                stopSelf();
            }
        }, new IntentFilter("No_More_Eating"));
    }

    public void onDestroy(){
        unregisterReceiver(stopSelfReceiver);
    }

    private class MyPhoneListener extends  PhoneStateListener{
        private String num;//记录来电号码

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING://来电振动
                    Toast.makeText(context, "Phone Call is coming!", Toast.LENGTH_SHORT).show();
                    num = incomingNumber;
                    if(AudioCapturer.isRecording()){
                        AudioRecordController.stopRecording(context, BluetoothScoIsStarted);
                        AudioCapturer.setInterruption();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:// 当接通电话开始通话时  可以进行录音
                    if(AudioCapturer.isRecording()){
                        AudioRecordController.stopRecording(context, BluetoothScoIsStarted);
                        AudioCapturer.setInterruption();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE://挂断电话时停止录音
                    if(AudioCapturer.isInterruped()) {
                        Toast.makeText(context, "Start Detecting", Toast.LENGTH_SHORT).show();
                        AudioRecordController.startRecording(context, BluetoothScoIsStarted);
                    }
                    break;
            }
        }
    }

    private void removeBlock(){
        blockStart.putExtra("Block", false);
        sendBroadcast(blockStart);
    }

}
