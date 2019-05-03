package com.eatingdetection.gy.ihearfood.FoodLog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kinse on 3/18/2016.
 */
public class S2Tservice extends Service {

    protected AudioManager mAudioManager;
    public SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    private String text = " ";
    private T2Sservice speech;
    private String TAG = "S2Tservice";

    private BroadcastReceiver receiverAtT2S = new BroadcastReceiver() {

        private boolean msg;
        Intent msgIntent = new Intent();
        String[] dataArray = new String[4];
        private int isWaiting = 0;

        @Override
        public void onReceive(Context context, Intent intent) {

            Timer timer = new Timer();
            TimerTask task = initialTask();

            if(intent.getAction().equals("Audio_Detection_Result")){
                msg = intent.getExtras().getBoolean("isEating");
                if (msg) {
                    Log.d(TAG, "start ---------->");
                    isWaiting = isWaiting + 1;
                    if(isWaiting == 1) {
                        dataArray[0] = formatTime();
                    }
                }
                else{
                    Log.d(TAG, "end ---------->");
                    dataArray[1] = formatTime();
                    Log.e("TimerTask", "befor delay" + formatTime());
                    //5 min = 300000 ms
                    timer.schedule(task,10000);
                }
            }

            if(intent.getAction().equals("startRecording")){
                Log.d(TAG, "Get start Recording");
                initialRecognizer();
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

            }

            if(intent.getAction().equals("stopRecording")){
                Log.d(TAG, "Get stop recording");
                mSpeechRecognizer.stopListening();
                mSpeechRecognizer.cancel();

                if (text.equals("no")){
                    dataArray[2] = "Not Eating";
                    dataArray[3] = text;
                }
                else if(text.equals("yes") || text.equals("yeah")){
                    Log.d(TAG, "yes");
                    dataArray[3] = text;
                    speech.speakOut(1);

                    msgIntent.setAction("startRecording");
                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    sendBroadcast(msgIntent);
                }
                else {
                    dataArray[2] = text;
                }

                LogData data = new LogData(dataArray);
                if(data.iscompleted()) {
                    new Record().writeToFile(data);

                    Intent updateIntent = new Intent();
                    updateIntent.setAction("LOG_UPDATA");
                    sendBroadcast(updateIntent);

                    Log.d(TAG, "Data check: " + data.getStartTime() + " ::: " + data.getEndTime() + " ::: " + data.getFoodInfo() + " ;;; " + data.getIsConfirmed());
                    data.empty();
                }
            }

        }

        private TimerTask initialTask(){
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if(isWaiting == 1) {
                        Log.e("TimerTask", "The Time Task is running " + formatTime());
                        speech.speakOut(0);

                        msgIntent.setAction("startRecording");
                        sendBroadcast(msgIntent);
                    } else if (isWaiting > 1){
                        isWaiting = isWaiting - 1;
                        Log.e("TimerTask", "Reschedule" + formatTime());

                    }
                }
            };
            return timerTask;
        }
    };

    public static String formatTime(){
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        return currentTime;
    }

    public void setText(String temp){
        text = temp;
    }

    //override for Service
    @Override
    public void onCreate() {
        super.onCreate();
        speech = new T2Sservice(this);

        //initialRecognizer();

        //
        //mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        //
        IntentFilter filter = new IntentFilter();
        filter.addAction("Audio_Detection_Result");
        filter.addAction("startRecording");
        filter.addAction("stopRecording");
        //filter.addAction("No_Network");
        registerReceiver(receiverAtT2S,filter);
    }

    private void initialRecognizer(){
        //Initial Speech to Text
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverAtT2S);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //function for S2T
    class SpeechRecognitionListener implements RecognitionListener
    {
        private String TAG = "SpeechRecognitionListener";
        public String tempText;
        @Override
        public void onBeginningOfSpeech()
        {
            //Log.d(TAG, "on Beginning of speech");
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {
            //Log.d(TAG, "on Buffer Received");
        }

        @Override
        public void onEndOfSpeech()
        {
            //Log.d(TAG, "on end of speech");

        }

        @Override
        public void onError(int error)
        {
            //Log.d(TAG, "on Error");
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client side error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Insufficient permissions";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "No match";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "error from server";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "No speech input";
                    break;
                default:
                    message = "Didn't understand, please try again.";
                    break;
            }

            Log.e(TAG, message);
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {
            //Log.d(TAG, "on event");
        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {
            //Log.d(TAG, "on partial results");
        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            //Log.e(TAG, "on Ready for speech");

        }

        @Override
        public void onResults(Bundle results)
        {
            //Log.d(TAG, "on Result");
            try {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (!matches.isEmpty()) {
                    tempText = matches.get(0);
                    //Log.e(TAG, tempText);
                    setText(tempText);

                    Intent i = new Intent();
                    i.setAction("stopRecording");
                    sendBroadcast(i);
                }
            } catch(NullPointerException e){
                e.printStackTrace();
            }

        }

        @Override
        public void onRmsChanged(float rmsdB)
        {
            //Log.d(TAG, "on Rms changed");
        }

    }


}