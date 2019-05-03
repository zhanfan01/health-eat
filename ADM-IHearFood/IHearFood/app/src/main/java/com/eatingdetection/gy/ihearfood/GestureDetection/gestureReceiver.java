package com.eatingdetection.gy.ihearfood.GestureDetection;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.json.JSONException;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.eatingdetection.gy.ihearfood.R;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.getpebble.android.kit.Constants;

public class gestureReceiver extends BroadcastReceiver {

    public final static UUID PEBBLE_APP_UUID = UUID.fromString("36d2bc31-b6a6-4a1d-a4cd-698c1c095e4a");
    private final static String TAG = "PebbleBroadcastReciever";

    protected static final String KEY_TYPE = "Key_Type";
    protected static final int fromReceiver = 41;
    protected static final int fromDetector = 43;
    protected static final String START_RECORD = "com.eatingdetection.gy.ihearfood.Start_Record";

    private Context mContext;
    private static Boolean mServiceStarted = false;

    // for tthreshold detect eat
    private static int DetectJitter = 0;
    private static Boolean isDetected = false;
    private static final int BUFFER_LIMIT = 144;  // 4 seconds buffer
    private static ArrayList<Float> mBuffer = new ArrayList<Float>();
    private static int mCount = 0;
    private static int mRecCount = 0;
    private static Classifier mModel, mModel1;
    private NotesDbAdapter mDbHelper;
    private static long mtime, currentTime, period;

    private Intent startRecordIntent = new Intent(START_RECORD);

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        File tFile = null;
        // not detect for 2 seconds or DetectJitter seconds if detect is ongoing
        if (DetectJitter > 0) {
            System.out.println("jin lai detectjitter, DetectJitter= " + DetectJitter);
            if (DetectJitter == 0) {
                isDetected = false;
            }
            DetectJitter--;
        }

        Calendar c = Calendar.getInstance();
        if (mRecCount == 0) {
            mtime = c.getTimeInMillis();
        }
        currentTime = c.getTimeInMillis();
        period = currentTime - mtime;

        mRecCount++;
        if (intent.getAction().equals(Constants.INTENT_APP_RECEIVE)) {
            final UUID receivedUuid = (UUID) intent.getSerializableExtra(Constants.APP_UUID);

            // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts containing their UUID
            if (!PEBBLE_APP_UUID.equals(receivedUuid)) {
                Log.i(TAG, "is not my UUID");
                return;
            }

            final int transactionId = intent.getIntExtra(Constants.TRANSACTION_ID, -1);
            final String jsonData = intent.getStringExtra(Constants.MSG_DATA);
            if (jsonData == null || jsonData.isEmpty()) {
                Log.i(TAG, "jsonData null");
                return;
            }

            try {
                final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
                // get accelerometer data
                if (data.getBytes(45) == null) {
                    System.out.println("OnReceiver: " + "45 = null");
                    return;
                }
                short[] shorts = new short[data.getBytes(45).length / 2];
                ByteBuffer.wrap(data.getBytes(45)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
                SimpleDateFormat tDateformat1 = new SimpleDateFormat("MMdd");
                String tDate1 = tDateformat1.format(new Date());
                FileOutputStream tBW = null;
                String tRecord = "";
                try {
                    //use MMddHH as file name for convience of upload
                    tFile = openFileOutput(getDate((data.getInteger(44) + 18000) * 1000, "MMddHH"));
                    tBW = new FileOutputStream(tFile, true);
                    for (int i = 0; i < shorts.length / 3; i++) {
                        tRecord = tRecord + getDate((data.getInteger(44) + 18000) * 1000, // 14400 = 4 hours
                                "MM/dd HHmmss") + " " + Short.toString((shorts[i * 3 + 0])) + " " +
                                Short.toString((shorts[i * 3 + 1])) + " "
                                + Short.toString((shorts[i * 3 + 2])) + "\r\n";
                        // buffer to detect eating
                        if (mBuffer.size() < BUFFER_LIMIT * 3) {
                            mBuffer.add(Float.parseFloat(Short.toString((shorts[i * 3 + 0]))));
                            mBuffer.add(Float.parseFloat(Short.toString((shorts[i * 3 + 1]))));
                            mBuffer.add(Float.parseFloat(Short.toString((shorts[i * 3 + 2]))));
                        } else {
                            if (DetectJitter == 0) {
                                try {
                                    DataSource source = new DataSource(mContext.getAssets().open("Trainyex.arff"));
                                    Instances dataset = source.getDataSet();
                                    dataset.setClassIndex(dataset.numAttributes() - 1);
                                    mModel1 = (Classifier) new SMO();
                                    mModel1.buildClassifier(dataset);
                                } catch (Exception e) {
                                    System.out.println("Can't read arff file");
                                    e.printStackTrace();
                                }
                                DEThreshold tDetect1 = new DEThreshold(mBuffer, mModel1);
                                mCount = mCount + tDetect1.getDetected();
                                DetectJitter = 2;
                            }
                            mBuffer.clear();
                        }
                    }
                    if (mCount > 2 || period > 20000) {
                        mRecCount = 0;
                        if (mCount > 2) {
                            Log.d(TAG, "Send start record intent");
                            context.sendBroadcast(startRecordIntent);

                            Intent mServiceIntent = new Intent(context, UploadEngine.class);
                            mServiceIntent.putExtra(KEY_TYPE, fromDetector);
                            context.startService(mServiceIntent);
                            isDetected = true;
                            mCount = 0;
                        }
                        mCount = 0;
                    }
                    System.out.println("Receiver: " + "mRecCount" + mRecCount + "mCount" + mCount);

                    tBW.write(tRecord.getBytes());
                    tBW.flush();
                    tBW.close();
                } catch (FileNotFoundException pException) {
                    Log.e(TAG, "findFile AccelerometerRecord" + ",message=" + pException.getMessage());
                } catch (IOException pException) {
                    Log.e(TAG, "writeFile AccelerometerRecord" + ",message=" + pException.getMessage());
                }

                //get label
                String label = data.getString(47);
                String tlabel = "";
                if (!label.equals("")) {
                    System.out.println("label is " + label);

                    // get confirmed detection, eating detected
                    if (label.equals("confirm")) {
                        mDbHelper = new NotesDbAdapter(context);
                        mDbHelper.open();
                        mDbHelper.createNote("Eating Detected", " ", getDate((data.getInteger(44) + 14400) * 1000,
                                "MM/dd/yyyy HH:mm"));
                        DetectJitter = 480;
                    }
                    if (label.equals("evernote")) {
                        Intent Telegramintent = new Intent();
                        Telegramintent.setAction("org.telegram.start");
                        Telegramintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(Telegramintent);
                    }
                    //............

                    try {
                        tFile = openFileOutput(tDate1 + "25");
                        tBW = new FileOutputStream(tFile, true);
                        tlabel = getDate((data.getInteger(44) + 18000) * 1000,
                                "dd/MM/yyyy HHmmss") + label + "\r\n";
                        tBW.write(tlabel.getBytes());
                        tBW.flush();
                        tBW.close();
                    } catch (FileNotFoundException pException) {
                        Log.e(TAG, "findFile AccelerometerRecord" + ",message=" + pException.getMessage());
                    } catch (IOException pException) {
                        Log.e(TAG, "writeFile AccelerometerRecord" + ",message=" + pException.getMessage());
                    }
                }
                //get label - end

                // record detected eating
                if (isDetected) {

                    mDbHelper = new NotesDbAdapter(context);
                    mDbHelper.open();
                    mDbHelper.createNote("Eating Detected", " ", getDate((data.getInteger(44) + 18000) * 1000,
                            "MM/dd/yyyy HH:mm"));
                    mDbHelper.close();

                    try {
                        tFile = openFileOutput(tDate1 + "25");
                        tBW = new FileOutputStream(tFile, true);
                        tlabel = getDate((data.getInteger(44) + 18000) * 1000,
                                "dd/MM/yyyy HHmmss") + "Eating Detected" + "\r\n";
                        tBW.write(tlabel.getBytes());
                        tBW.flush();
                        tBW.close();
                    } catch (FileNotFoundException pException) {
                        Log.e(TAG, "findFile AccelerometerRecord" + ",message=" + pException.getMessage());
                    } catch (IOException pException) {
                        Log.e(TAG, "writeFile AccelerometerRecord" + ",message=" + pException.getMessage());
                    }

                    Intent iDetect = new Intent();
                    iDetect.setAction("com.swin.smarteater.action.DETECT");
                    context.sendBroadcast(iDetect);
                    isDetected = false;
                }
                // record detected eating - end

                // send ack back to Pebble
                PebbleKit.sendAckToPebble(context, transactionId);

                //Start Service
                if (!mServiceStarted) {
                    Intent mServiceIntent = new Intent(context, UploadEngine.class);
                    mServiceIntent.putExtra(KEY_TYPE, fromReceiver);
                    if (context.startService(mServiceIntent) != null) {
                        mServiceStarted = true;
                    }

                }
            } catch (JSONException e) {
                Log.i(TAG, "failed reived -> dict" + e);
                return;
            }
        }
        // end of if (intent.getAction().equals(Constants.INTENT_APP_RECEIVE))

        if (intent.getAction().equals("com.swin.smarteater.action.DETECT")) {
            System.out.println("Got YEX DETECT!");
        }

    }

    private final File openFileOutput(String pFileName)
            throws FileNotFoundException, IOException {
        String tStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(tStorageState)) {
            File tStorageDirectory = Environment.getExternalStorageDirectory();
            String tDirectoryPath = tStorageDirectory.getPath()
                    + File.separator + mContext.getPackageName();
            File tDirectory = new File(tDirectoryPath);
            if (!tDirectory.exists()) {
                tDirectory.mkdir();
            }
            return new File(tDirectoryPath + File.separator + pFileName);
//      return new FileOutputStream(tFile,true);
        } else {
//      return mContext.openFileOutput(pFileName,Context.MODE_APPEND);
            return new File(mContext.getFilesDir() + File.separator + pFileName);
        }
    }

    private static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private float roll(float x, float y, float z) {

        float result = (float) Math.toDegrees(Math.atan2(z, y));   // atan2(x,y)= y/x,we want y/z
        return result;
    }

    private float pitch(float x, float y, float z) {

        float result = (float) Math.toDegrees(Math.atan2(Math.sqrt(y * y + z * z), -x));   // atan2(x,y)= y/x,we want y/z
        return result;
    }

    private boolean isGravity(float x, float y, float z) {
        if (995 < Math.sqrt(x * x + y * y + z * z) && Math.sqrt(x * x + y * y + z * z) < 1005) {
            return true;
        } else {
            return false;
        }
    }

}
