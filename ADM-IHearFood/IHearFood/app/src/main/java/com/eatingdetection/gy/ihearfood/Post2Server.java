package com.eatingdetection.gy.ihearfood;

/**
 * Created by GY on 3/18/2016.
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Post2Server {
    public static final String TAG = "Post";
    public static final String ADD_URL = "http://129.63.16.66:8000/inputstream/";
    public static String output;

    public static boolean Post(double[] features) throws IOException {
        boolean result;

        HttpURLConnection connection = null;
        try {
            // 创建连接
            URL url = new URL(ADD_URL);
            connection = (HttpURLConnection) url.openConnection();

            // 设置http连接属性

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST"); // 可以根据需要 提交
            // GET、POST、DELETE、INPUT等http提供的功能
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);

            // 设置http头 消息
            connection.setRequestProperty("Content-Type", "application/json"); // 设定
            // 请求格式
            // json，也可以设定xml格式的
            // connection.setRequestProperty("Content-Type", "text/xml"); //设定
            // 请求格式 xml，
            connection.setRequestProperty("Accept", "application/json");// 设定响应的信息的格式为
            // json，也可以设定xml格式的
            // connection.setRequestProperty("X-Auth-Token","xx");
            // //特定http服务器需要的信息，根据服务器所需要求添加
            connection.connect();

            // 添加 请求内容
            JSONObject feature = new JSONObject();
            String name;
            double value;
            try {
                for(int i = 0; i < features.length; i++){
                    name = "att" + Integer.toString(i);
                    value = features[i];
                    feature.put(name, value);
                }
            } catch (JSONException e) {
                Log.e(TAG, "unexpected JSON exception", e);
            }

            //Log.d(TAG, feature.toString());
            OutputStream out = connection.getOutputStream();
            out.write(feature.toString().getBytes());
            out.flush();
            out.close();

            // 读取响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            StringBuffer sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }
            //System.out.println(sb);
            output = sb.substring(10, 14);

            reader.close();

            //Log.d(TAG, "Get result: " + output);

            // // 断开连接
            connection.disconnect();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if(output.equals("true")){
            result = true;
        }else{
            result = false;
        }

        //Log.d(TAG, "Get result: " + result);
        return result;

    }

}
