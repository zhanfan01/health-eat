package com.eatingdetection.gy.ihearfood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by GY on 3/19/2016.
 */
public class AudioRecordController {

    private static final String TAG = "AudioRecordController";

    private static AudioManager mAudioManager = null;
    private static AudioCapturer mRecorder = null;

    static int count = 0;

    static BroadcastReceiver receiver;

    public static void startRecording(Context mContext, Boolean BluetoothScoIsStarted) {

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        if(!BluetoothScoIsStarted) {
            if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
                Log.d(TAG, "No Bluetooth");
                Toast.makeText(mContext, "No Bluetooth Available", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "starting bluetooth");
                mAudioManager.startBluetoothSco();
                BluetoothScoIsStarted = true;
            }
        }

        mRecorder = new AudioCapturer();
/*
        // Use MediaRecorder for audio recording
        Log.d(LOG_TAG, mAudioName);

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mRecorder.setOutputFile(mAudioName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "prepare() failed");
            Log.d(LOG_TAG, e.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            Log.d(LOG_TAG, e.toString());
        }
*/
        mContext.registerReceiver( receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.d(TAG, "Audio SCO state: " + state);

                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                /*
                 * Now the connection has been established to the bluetooth device.
                 * Record audio or whatever (on another thread).With AudioRecord you can record with an object created like this:
                 * new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 * AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
                 *
                 * After finishing, don't forget to unregister this receiver and
                 * to stop the bluetooth connection with am.stopBluetoothSco();
                 */
                    mAudioManager.setBluetoothScoOn(true);
                    Log.d(TAG, "SCO Audio connected");
                    Toast.makeText(context, "SCO Audio connected", Toast.LENGTH_SHORT).show();
                    mRecorder.startCapture(context);
                    context.unregisterReceiver(this);
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mAudioManager.startBluetoothSco();

                    receiveCounter(context);

                    context.sendBroadcast(new Intent("No_More_Eating"));
/*
                    Log.d(TAG, "SCO Audio Unconnected");
                    Toast.makeText(context, "SCO Audio Unconnected", Toast.LENGTH_SHORT).show();
                    mRecorder.stopReccord();
                    context.unregisterReceiver(this);
 */
                }

            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

    }

    private static void receiveCounter(Context context){
        if(count > 2){
            Log.d(TAG, "SCO Audio Unconnected");
            Toast.makeText(context, "SCO Audio Unconnected", Toast.LENGTH_SHORT).show();
            mRecorder.stopReccord();
            count = 0;
            context.unregisterReceiver(receiver);
        }
        count++;
    }

    public static void stopRecording(Context mContext, Boolean BluetoothScoIsStarted) {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mRecorder.stopReccord();
        /*
        if(mAudioManager.isBluetoothScoOn()){
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            BluetoothScoIsStarted = false;
        }
        */
    }
}
