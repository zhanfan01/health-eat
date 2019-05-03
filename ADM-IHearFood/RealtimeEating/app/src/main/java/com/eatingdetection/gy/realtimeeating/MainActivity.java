package com.eatingdetection.gy.realtimeeating;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Environment;
import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "RTEating";

    private AudioManager mAudioManager = null;

    private Button mRecordButton = null;
    private AudioCapturer mRecorder = null;

    private boolean BluetoothScoIsStarted = false;

    private Intent intent = null;
    Context context = null;

    /*
    private static final String[] Food_Types = new String[]{
            "Pizza", "Fries", "Pasta", "Yogurt", "Pudding", "Soup", "Burger", "Sandwich", "Steak",
            "Whole Wheat Bread", "Apple", "Carrot", "Salad", "Potato Chips"
    };
    */

    private void startRecording() {
        if(!BluetoothScoIsStarted) {
            if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
                Log.d(LOG_TAG, "No Bluetooth");
                Toast.makeText(this, "No Bluetooth Available", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(LOG_TAG, "starting bluetooth");
                mAudioManager.startBluetoothSco();
                BluetoothScoIsStarted = true;
            }
        }

        mRecorder = new AudioCapturer();
/*
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
        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.d(LOG_TAG, "Audio SCO state: " + state);

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
                    Log.d(LOG_TAG, "SCO Audio connected");
                    Toast.makeText(context, "SCO Audio connected", Toast.LENGTH_SHORT).show();
                    mRecorder.startCapture();
                    unregisterReceiver(this);
                } else {
                    Log.d(LOG_TAG, "SCO Audio Unconnected");
                    Toast.makeText(context, "SCO Audio Unconnected", Toast.LENGTH_SHORT).show();
                    mRecorder.stopReccord();
                    unregisterReceiver(this);
                }

            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));

    }

    public void stopRecording() {
        mRecorder.stopReccord();
        if(mAudioManager.isBluetoothScoOn()){
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
            BluetoothScoIsStarted = false;
        }
    }

    //定义一个BroadcastReceiver
    private BroadcastReceiver updatUIReceiver = new BroadcastReceiver() {
        //当service发出广播后，此方法就可以得到service传回来的值
        @Override
        public void onReceive(Context context, Intent intent) {
            //更新界面。这里改变Button的值
            //得到intent返回来的值,0表示此时是播放，1表示暂停, 2是停止
            int backFlag = intent.getExtras().getInt("backFlag");
            switch(backFlag){
                case 0:
                    mRecordButton.setText("暂停");
                    break;
                case 1:
                case 2:
                    mRecordButton.setText("播放");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if(!mAudioManager.isBluetoothScoAvailableOffCall()){
            Log.d(LOG_TAG, "No Bluetooth");
            Toast.makeText(this, "No Bluetooth Available", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(LOG_TAG, "starting bluetooth");
            mAudioManager.startBluetoothSco();
            BluetoothScoIsStarted = true;
        }

        mRecordButton = (Button) findViewById(R.id.record_btn);
        mRecordButton.setText(R.string.record);
        mRecordButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (AudioCapturer.isStopped) {
                    startRecording();
                    //AudioRecordController.startRecording(context, mAudioManager);
                    mRecordButton.setText(R.string.stop);
                } else {
                    stopRecording();
                    //AudioRecordController.stopRecording();
                    mRecordButton.setText(R.string.record);

                }
            }
        });
    }
}
