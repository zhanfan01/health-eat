package com.eatingdetection.gy.ihearfood.FoodLog;

import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by kinse on 3/18/2016.
 */
public class Record {

    public final String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "SmartFood";
    public final String filename = filePath + File.separator + "FoodLog.txt";
    private String TAG = "Record";
    public Record() {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
                Log.e(TAG, "File doesn't exist");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public void writeToFile(LogData data) {
        File file = new File(filePath);
        if(!file.exists())
            file.mkdirs();

        file = new File(filename);
        if(!file.exists()){
            Log.e(TAG, "File doesn't exist");
        }

        FileOutputStream outputStream = null;
        try{
            outputStream = new FileOutputStream(file, true);
            String msg = data.startTime + "%" + data.endTime + "%" + data.foodInfo + "%" + data.isConfirmed + "\n";
            outputStream.write(msg.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return;
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public ArrayList<LogData> readFromFile(){
        ArrayList<String> log = new ArrayList<String>();
        ArrayList<LogData> finalLog = new ArrayList<LogData>();

        try {
            File file = new File(filename);
            FileInputStream fin = new FileInputStream(file);
            DataInputStream dio = new DataInputStream(fin);

            String strline = dio.readLine();
            while( strline!= null){
                log.add(strline);
                strline = dio.readLine();
            }

        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        for(int i=0; i<log.size(); i++){
            finalLog.add(new LogData(log.get(i).split("%")));
        }
        return finalLog;
    }

    public LogData getLastData(){
        ArrayList<LogData> datas = readFromFile();
        LogData lastData = datas.get(datas.size()-1);
        return lastData;
    }
}
