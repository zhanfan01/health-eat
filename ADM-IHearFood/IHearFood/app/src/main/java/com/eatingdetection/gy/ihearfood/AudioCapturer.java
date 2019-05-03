package com.eatingdetection.gy.ihearfood;

/**
 * Created by GY on 3/13/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.eatingdetection.gy.ihearfood.AudioFeatures.FeatureExtractor;
import com.eatingdetection.gy.ihearfood.FoodLog.T2Sservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AudioCapturer {
    private static final String TAG = "AudioCapturer";

    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT;
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int SAMPLE_RATE = 8000;                                        // Audio sample rate 8000Hz
    private static final int BIT_WIDE = 16;                                             // PCM 16 bit
    private static final int NUM_CHANNEL = 1;                                           // MONO channel (single channel)
    private static final int WINDOW_SIZE = 3;                                           // Audio window size: 3 sec

    private AudioRecord mAudioRecord = null;
    private int mMinBufferSize = 0;
    private int BufferSize = SAMPLE_RATE * BIT_WIDE * WINDOW_SIZE * NUM_CHANNEL / 8;

    private Thread mCaptureThread;
    private Thread mAutoStopthread;
    private static boolean isRecording = false;
    private static boolean isInterrupted = false;
    private volatile boolean isLoopExit = false;
    //private boolean onStart = true;
    private boolean isEating;
    Context context;

    Intent resultIntent = new Intent("Audio_Detection_Result");
    Intent stopIntent = new Intent("No_More_Eating");
    //Intent noNetworkIntent = new Intent("No_Network");
    Calendar mCalendar = null;
    Date mDate = null;
    private boolean[] resultbuffer = {true, true, true, true, true};

    //AudioName裸音频数据文件
    private static final String AudioName = "/sdcard/love.raw";
    //NewAudioName可播放的音频文件
    private static final String NewAudioName = "/sdcard/new";

    public static boolean isRecording(){
        return isRecording;
    }

    public static boolean isInterruped() {
        return isInterrupted;
    }

    public static void setInterruption() {
        isInterrupted = true;
    }

    public boolean startCapture(Context mContext) {
        return startRecord(AUDIO_SOURCE, SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT, BufferSize, mContext);
    }

    public boolean startRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, Context mContext) {

        context = mContext;

        if (isRecording) {
            Log.e(TAG, "Capture already started!");
            return false;
        }

        //onStart = true;
        isEating = false;

        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter!");
            return false;
        }
        //Log.d(TAG, "getMinBufferSize = " + mMinBufferSize + " bytes !");

        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        if (mAudioRecord.getState() == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "AudioRecord initialize fail!");
            return false;
        }

        mAudioRecord.startRecording();

        Toast.makeText(context, "Audio Recording!", Toast.LENGTH_LONG).show();

        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();

        mAutoStopthread = new Thread(new AutoStopRunnable());
        mAutoStopthread.start();

        isRecording = true;

        Log.d(TAG, "Start audio capture successfully!");

        return true;
    }

    public void stopReccord() {
        if (!isRecording) {
            return;
        }

        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
            mAutoStopthread.interrupt();
            isLoopExit = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }

        mAudioRecord.release();
        mAudioRecord = null;
        isRecording = false;

        //Toast.makeText(context, "Stop Audio Recording", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Stop audio capture success !");
    }

    private class AudioCaptureRunnable implements Runnable {

        double[] features;
        boolean result;
        int i = 0;
        int j = 0;

        @Override
        public void run() {

/*
                // Initialize output file stream
                FileOutputStream fos = null;
                //Open audio file
                try {
                    File file = new File(AudioName);
                    if (file.exists()) {
                        file.delete();
                    }
                    fos = new FileOutputStream(file);// 建立一个可存取字节的文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
*/

            while (!isLoopExit) {

                byte[] buffer = new byte[BufferSize];

                int ret = mAudioRecord.read(buffer, 0, BufferSize);

                long t1 = SystemClock.currentThreadTimeMillis();

                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "Error ERROR_BAD_VALUE");
                } else {
/*
                    // Write audio file
                    try {
                        fos.write(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
*/
                    // Log.d(TAG, "Calculating features");
                    features = FeatureExtractor.getFeatures(buffer, SAMPLE_RATE);
                    // Log.d(TAG, "features done");

                    long t2 = SystemClock.currentThreadTimeMillis();
                    Log.d(TAG, "Features Extraction Time Cost: " + Long.toString(t2 - t1) + "ms");

                    ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkInfo WIFIinfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    boolean isWifiAvail = WIFIinfo.isAvailable();
                    boolean isWifiConn = WIFIinfo.isConnected();
                    NetworkInfo cellularinfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    boolean isMobileAvail = cellularinfo.isAvailable();
                    boolean isMobilConn = cellularinfo.isConnected();

                    if(isWifiConn || isMobilConn) {
                        try {
                            result = Post2Server.Post(features);
                        } catch (Exception e) {
                            Log.e(TAG, "Connect to server fail", e);
                        }
                    }else {
                        Log.d(TAG, "No Internet");
                        T2Sservice.mNotification("Please connect you phone to Internet");
                        context.sendBroadcast(stopIntent);
                        //noNetworkIntent.putExtra("noInternet", "Please connect to Internet");
                    }

                    long t3 = SystemClock.currentThreadTimeMillis();
                    Log.d(TAG, "Online Detection Time Cost: " + Long.toString(t3 - t2) + "ms");

                    //Log.d(TAG, Boolean.toString(result));
                    if (result) {
                        Log.d(TAG, "Result: True");
                        if(!isEating){
                            resultIntent.putExtra("isEating", result);
                            context.sendBroadcast(resultIntent);
                            isEating = true;
                            T2Sservice.mNotification("Eating is detected");
                        }
                    } else {
                        Log.d(TAG, "Result: false");
                    }

                    resultbuffer[i] = result;
                    i = (i + 1) % 5;
                    j++;
                }
                Log.d(TAG, "OK, Captured " + ret + " bytes !");
            }
/*
                    // Close audio file
                    try {
                        fos.close();// 关闭写入流
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    copyWaveFile(AudioName, NewAudioName + Integer.toString(j) + ".wav");
*/

        }
    }

    // Stop recording
    private class AutoStopRunnable implements Runnable {

        @Override
        public void run() {
            while (!isLoopExit) {

                if (!(resultbuffer[0] || resultbuffer[1] || resultbuffer[2] || resultbuffer[3] || resultbuffer[4])) {
                    Log.d(TAG, "No Eating Detected");
                    stopReccord();
                    if(isEating) {
                        resultIntent.putExtra("isEating", false);
                        context.sendBroadcast(resultIntent);
                        isEating = false;
                    }
                    context.sendBroadcast(stopIntent);
                }
            }

        }
    }
/*
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = 0;
        long longSampleRate = SAMPLE_RATE;
        int channels = NUM_CHANNEL;
        long byteRate = BIT_WIDE * SAMPLE_RATE * channels / 8;
        byte[] data = new byte[BufferSize];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
/*
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
*/
}

