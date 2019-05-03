package com.eatingdetection.gy.realtimeeating;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * Created by GY on 3/19/2016.
 */
public class AudioService extends Service {

    Context context = getApplicationContext();

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        System.out.println("开始监听电话服务状态");
        //获得电话管理器
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //为管理器设置监听器,监听电话的呼叫状态
        manager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    private class MyPhoneListener extends  PhoneStateListener{
        private String num;//记录来电号码

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING://来电振动
                    Toast.makeText(context, "Phone Call is coming", Toast.LENGTH_SHORT).show();
                    num = incomingNumber;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:// 当接通电话开始通话时  可以进行录音
                    stopSelf();
                    break;
                case TelephonyManager.CALL_STATE_IDLE://挂断电话时停止录音

                    break;
            }
        }
    }
}
