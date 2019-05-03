package com.eatingdetection.gy.ihearfood.GestureDetection;



import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;


public class UploadEngine extends Service {
  
  protected static final String TAG = "CollectorEngine";
  protected static final String KEY_TYPE = "Key_Type";
  protected static final String KEY_PATH = "Key_Path";
  protected static final int fromReceiver = 41;
  protected static final int fromDetector = 43;
  protected static final int fromAlarm = 42;
  protected static final int fromBluetooth = 44;
  
  /**********  File Path *************/
  protected static final String upLoadServerUri = "http://129.63.16.134/play/upload";
  protected static int serverResponseCode = 0;
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private AlarmManager mAlarmMgr;
	private PendingIntent mAlarmIntent;
	private PendingIntent mAlarmIntent1;
	private File mPhoneFile = null;
	private static String mPebbleName = null;
	private PebbleDataReceiver receiver;
	
	private Handler handler = new Handler();
	private UUID uuid = UUID.fromString("1d401fc9-9162-455c-84c8-303197e2661f");
	private boolean isRegistered = false; 
	

	 // the tuple key corresponding to the detection notification on the watch
  private static final int DETECT_KEY = 48;

	//Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
     public ServiceHandler(Looper looper) {
         super(looper);
     }
     @Override
     public void handleMessage(Message msg) {
    		 // upload files from phone to server
    	 int uploadlimit = 4;
    	 if(isConnectedViaWifi()){
	    	 if(mPhoneFile != null){
	    		 System.out.println(mPhoneFile);
	    		 File[] tFiles = mPhoneFile.listFiles();
	    		 SimpleDateFormat tDateformat = new SimpleDateFormat("MMddHH");
	         String tDate = tDateformat.format(new Date());
	         for(File tFile : tFiles) {
	        	 if(tFile.isFile()){
	        		 final String uploadFile = tFile.getAbsolutePath();
		           int tFileName = 999999;
		           try {
		             tFileName = Integer.parseInt(tFile.getName());
		             } catch (NumberFormatException e) {
		                 // Ignore as if it's not a number we don't care
		             }
		           if(tFileName<Integer.parseInt(tDate) && uploadlimit >0){
		           	 new Thread(new Runnable(){
		           		 public void run(){
		           			 System.out.println("Gonna upload: "+uploadFile);
		           			 uploadFile(uploadFile);
		           		 }
		           	 }).start();
		           }
		           uploadlimit--;
	        	 }
	        	 
	     	   }
	    	 }// end of if mphonefile
      }
    }//end of handleMessage
  }
  
  @Override
  public IBinder onBind(Intent pIntent) {
    return null;
  }
  
  @Override
  public void onCreate() {
  	
  	System.out.println("Oncreate get in!");
  	
//  // code for Bluetooth connect
	  BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	  if (mBluetoothAdapter != null) {
		    // Device does not support Bluetooth
		  Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		  // If there are paired devices
		  if (pairedDevices.size() > 0) {
		   // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		      // Add the name and address to an array adapter to show in a ListView
			 	  if(device.getName().contains("Pebble")){
			 		  mPebbleName = device.getName().replace(" ",":");
			 	  }
		    }
		  }
	  }
	  
  	try {
  		mPhoneFile = openFileOutput();
    } catch(FileNotFoundException pException) {
      Log.e(TAG, "findFile AccelerometerRecord" + ",message=" + pException.getMessage());
    } catch(IOException pException) {
    	Log.e(TAG, "writeFile AccelerometerRecord" + ",message=" + pException.getMessage());
    }
  	HandlerThread thread = new HandlerThread("ServiceStartArguments",
  			Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    
 // Get the HandlerThread's Looper and use it for our Handler
    mServiceLooper = thread.getLooper();
    mServiceHandler = new ServiceHandler(mServiceLooper);
    
    // Set alarm to upload every hour
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
    calendar.set(Calendar.MINUTE, 5);
    
    if (mAlarmMgr!= null) {
      mAlarmMgr.cancel(mAlarmIntent);
      mAlarmMgr.cancel(mAlarmIntent1);
    }
    
    mAlarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
    Intent alarmintent = new Intent(this, UploadEngine.class);
    alarmintent.putExtra(KEY_TYPE, fromAlarm);
    Intent alarmBluetooth = new Intent(this, UploadEngine.class);
    alarmBluetooth.putExtra(KEY_TYPE, fromBluetooth);
    mAlarmIntent = PendingIntent.getService(this, 0, alarmintent, 0);
    mAlarmIntent1 = PendingIntent.getService(this, 1, alarmBluetooth, 0);
    
  	
    mAlarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
        1000 * 60 * 60, mAlarmIntent);
    mAlarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
        1000 * 60 * 5, mAlarmIntent1);
  }
  
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
  	System.out.println("OnStartCommand get in!");
  	NotificationManager mNotificationManager =
  	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
  	// mId allows you to update the notification later on.
  	if(intent!= null && intent.hasExtra(KEY_TYPE)){
  		if(intent.getIntExtra(KEY_TYPE,0) == fromAlarm){
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);
      }
  		if(intent.getIntExtra(KEY_TYPE,0) == fromDetector){
//  			sendNotificationWatchApp();
//  			Notification.Builder tDetectNote =
//  	        new Notification.Builder(this)
//  	  	    .setSmallIcon(R.drawable.ic_launcher)
//  	        .setContentTitle("Eating has been detected.")
//  	        .setContentText("")
//  	        .setDefaults(Notification.DEFAULT_SOUND);
//		  	
//				mNotificationManager.notify(0, tDetectNote.build());
      }
  		if(intent.getIntExtra(KEY_TYPE,0) == fromBluetooth){
  			boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
  		  if(!connected){
//  		  	Notification.Builder tDetectNote =
//    	        new Notification.Builder(this)
//    	  	    .setSmallIcon(R.drawable.ic_launcher)
//    	        .setContentTitle("SmartEater can't work")
//    	        .setContentText("Connect Pebble and run the watch app.")
//    	        .setDefaults(Notification.DEFAULT_SOUND);  		  	
//  				mNotificationManager.notify(0, tDetectNote.build());
  				Intent iDetect = new Intent();
        	iDetect.setAction("edu.swin.walkmore.PEBBLE_STATE_CHANGE");
        	iDetect.putExtra("edu.swin.walkmore.PEBBLE_STATE", 0);
          this.sendBroadcast(iDetect);
  		  } else {
	  		  Intent iDetect = new Intent();
	      	iDetect.setAction("edu.swin.walkmore.PEBBLE_STATE_CHANGE");
	      	iDetect.putExtra("edu.swin.walkmore.PEBBLE_STATE", 1);
	        this.sendBroadcast(iDetect);
  		  }
  		
  		  
      }
  	}
      // If we get killed, after returning from here, restart
      return START_STICKY;
  }

  
  @Override
  public void onDestroy() {
  	if (mAlarmMgr!= null) {
      mAlarmMgr.cancel(mAlarmIntent);
    }
  	if(isRegistered){
			unregisterReceiver(receiver);
			isRegistered = false;
		}
  }
  
  public int uploadFile(String sourceFileUri) {
    
    String fileName = sourceFileUri;
    HttpURLConnection conn = null;
    DataOutputStream dos = null;  
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 5 * 1024; 
    File sourceFile = new File(sourceFileUri);
    
      if (!sourceFile.isFile()) {
      	Log.e("uploadFile", "Source File not exist :"
                         + " " + fileName);
      	return 0;
      }
      else {
          try { 
               
                // open a URL connection to the Servlet
              FileInputStream fileInputStream = new FileInputStream(sourceFile);
              
              URL url = new URL(upLoadServerUri+"?id="+getAndroidId()+mPebbleName);
               
              // Open a HTTP  connection to  the URL
              conn = (HttpURLConnection) url.openConnection(); 
              conn.setDoInput(true); // Allow Inputs
              conn.setDoOutput(true); // Allow Outputs
              conn.setUseCaches(false); // Don't use a Cached Copy
              conn.setRequestMethod("POST");
              conn.setRequestProperty("Connection", "Keep-Alive");
              conn.setRequestProperty("ENCTYPE", "multipart/form-data");
              conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
              conn.setRequestProperty("uploaded_file", sourceFile.getName()); 
               
              dos = new DataOutputStream(conn.getOutputStream());
     
              dos.writeBytes(twoHyphens + boundary + lineEnd); 
              dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";" +
              		"filename=\"" + sourceFile.getName() + "\"" + lineEnd);
               
              dos.writeBytes(lineEnd);
     
              // create a buffer of  maximum size
              bytesAvailable = fileInputStream.available(); 
     
              bufferSize = Math.min(bytesAvailable, maxBufferSize);
              buffer = new byte[bufferSize];
     
              // read file and write it into form...
              bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                 
              while (bytesRead > 0) {
                   
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);   
                 
               }
     
              // send multipart form data necesssary after file data...
              dos.writeBytes(lineEnd);
              dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
     
              // Responses from the server (code and message)
              serverResponseCode = conn.getResponseCode();
              final String serverResponseMessage = conn.getResponseMessage();
                
              Log.d("uploadFile", "HTTP Response is : "
                      + serverResponseMessage + ": " + serverResponseCode);
               
              if(serverResponseCode == 200){
                 Log.d( "uploadFile", "File Upload Completed.\n\n"+
                               "\nservermsg: " + serverResponseMessage);
                 sourceFile.delete();
              }    
               
              //close the streams //
              fileInputStream.close();
              dos.flush();
              dos.close();
                
         } catch (MalformedURLException ex) {
              
             ex.printStackTrace();
             Log.e("Upload file to server", "error: " + ex.getMessage(), ex);  
         } catch (Exception e) {
              
             e.printStackTrace();
             Log.e("Upload file to server Exception", "Exception : "
                                              + e.getMessage(), e);  
         }
         return serverResponseCode; 
          
      } // End else block 
   } 
  
  private final File openFileOutput() 
      throws FileNotFoundException, IOException {
    String tStorageState = Environment.getExternalStorageState();
    if(Environment.MEDIA_MOUNTED.equals(tStorageState)) {
      File tStorageDirectory = Environment.getExternalStorageDirectory();
      String tDirectoryPath = tStorageDirectory.getPath()
          + File.separator + getApplicationContext().getPackageName();
      File tDirectory = new File(tDirectoryPath);
      if(!tDirectory.exists()) {
        tDirectory.mkdir();
      }
      return new File(tDirectoryPath + File.separator);
    } else {
      return new File(getApplicationContext().getFilesDir() + File.separator);
    }
  }
  
  private boolean isConnectedViaWifi() {
    ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);     
    System.out.println("wifi connected is:" + mWifi.isConnected());
    return mWifi.isConnected();
  }
  
  public String sendFileToServer(String filename, String targetUrl) {
    String response = "error";
    Log.e("Image filename", filename);
    Log.e("url", targetUrl);
    HttpURLConnection connection = null;
    DataOutputStream outputStream = null;
    // DataInputStream inputStream = null;

    String pathToOurFile = filename;
    String urlServer = targetUrl;
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");

    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1 * 1024;
    try {
        FileInputStream fileInputStream = new FileInputStream(new File(
                pathToOurFile));

        URL url = new URL(urlServer);
        connection = (HttpURLConnection) url.openConnection();

        // Allow Inputs & Outputs
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setChunkedStreamingMode(1024);
        // Enable POST method
        connection.setRequestMethod("POST");

        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type",
                "multipart/form-data;boundary=" + boundary);

        outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(twoHyphens + boundary + lineEnd);

        String connstr = null;
        connstr = "Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                + pathToOurFile + "\"" + lineEnd;
        Log.i("Connstr", connstr);

        outputStream.writeBytes(connstr);
        outputStream.writeBytes(lineEnd);

        bytesAvailable = fileInputStream.available();
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = new byte[bufferSize];

        // Read file
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        Log.e("Image length", bytesAvailable + "");
        try {
            while (bytesRead > 0) {
                try {
                    outputStream.write(buffer, 0, bufferSize);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    response = "outofmemoryerror";
                    fileInputStream.close();
                    return response;
                }
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response = "error";
            return response;
        }
        outputStream.writeBytes(lineEnd);
        outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                + lineEnd);

        // Responses from the server (code and message)
        int serverResponseCode = connection.getResponseCode();
        String serverResponseMessage = connection.getResponseMessage();
        Log.i("Server Response Code ", "" + serverResponseCode);
        Log.i("Server Response Message", serverResponseMessage);

        if (serverResponseCode == 200) {
            response = "true";
        }

        String CDate = null;
        Date serverTime = new Date(connection.getDate());
        try {
            CDate = df.format(serverTime);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Date Exception", e.getMessage() + " Parse Exception");
        }
        Log.i("Server Response Time", CDate + "");

        filename = CDate
                + filename.substring(filename.lastIndexOf("."),
                        filename.length());
        Log.i("File Name in Server : ", filename);

        fileInputStream.close();
        outputStream.flush();
        outputStream.close();
        outputStream = null;
    } catch (Exception ex) {
        // Exception handling
        response = "error";
        Log.e("Send file Exception", ex.getMessage() + "");
        ex.printStackTrace();
    }
    return response;
}
  
  public void sendNotificationWatchApp() {
    PebbleDictionary data = new PebbleDictionary();
    data.addUint8(DETECT_KEY, (byte) 1);

    // Send the assembled dictionary to the watch-app; this is a no-op if the app isn't running or is not
    // installed
    PebbleKit.sendDataToPebble(getApplicationContext(), gestureReceiver.PEBBLE_APP_UUID, data);
  }
  
  private String getAndroidId() {
    String tAndroidId = Settings.Secure.getString(this.getContentResolver(),
            Settings.Secure.ANDROID_ID);
    if(tAndroidId == null) {
        tAndroidId = "s0w18in54ma3r0o7";
    }
    return tAndroidId;
  }
  
}