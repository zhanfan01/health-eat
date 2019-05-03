package com.eatingdetection.gy.ihearfood.AudioFeatures;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by GY on 3/18/2016.
 */
public class WaveRead {
    public static double[] GetWave(byte[] buffer) {

        double[] wave = new double[buffer.length / 2];

        short[] shorts = shortMe(buffer);
        String myshorts = "";
        String mydouble = "";
/*
        for(int i = 0, j = 0; i < buffer.length; i+=2, j++){
            int data_low =  unsignedToBytes(buffer[i]);
            int data_high = unsignedToBytes(buffer[i+1]);

            double data_true = data_high * 256 + data_low;
            double data_complement = 0;
            int my_sign = (int)(data_high / 128);

            if (my_sign == 1)
            {
                data_complement = (data_true - 65536) / 256 - 1;
            }
            else
            {
                data_complement = data_true/258;
            }
            wave[j] = data_complement/(double)32768;
            if(j<300) {
                Log.d("WaveRead", Double.toString(data_complement));
            }
        }
*/
        for (int i = 0; i < buffer.length / 2; i++) {
            if (shorts[i] > 0) {
                shorts[i] = (short) (shorts[i] / 256);
            } else if (shorts[i] < 0) {
                shorts[i] = (short) (shorts[i] / 256 - 1);
            }
            else {
                shorts[i] = 0;
            }
            wave[i] = shorts[i] / (double)32768;
            if(i<300){
                myshorts = myshorts + Short.toString(shorts[i]) + ",";
                mydouble = mydouble + Double.toString(wave[i]) + ",";
            }
        }

        //Log.d("WaveRead", myshorts);
        //Log.d("WaveRead", mydouble);
        return wave;
    }

    public static int unsignedToBytes(byte a) {
        int b = (a & 0xFF);
        return b;
    }

    public static short[] shortMe(byte[] bytes) {
        short[] out = new short[bytes.length / 2]; // will drop last byte if odd number
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        for (int i = 0; i < out.length; i++) {
            out[i] = bb.getShort();
        }
        return out;
    }

}
