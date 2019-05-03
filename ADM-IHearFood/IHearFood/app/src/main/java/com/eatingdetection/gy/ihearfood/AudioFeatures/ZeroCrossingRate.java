package com.eatingdetection.gy.ihearfood.AudioFeatures;

/**
 * Created by GY on 3/16/2016.
 */
public class ZeroCrossingRate {
    public static double ZCR(double[] window) {
        int length = window.length;

        double[] win = new double[length];
        for (int i = 0; i < (length - 1); i++) {
            win[i + 1] = window[i];
        }

        double sum = 0;
        int a, b;
        for (int i = 0; i < length; i++) {
            if (window[i] > 0) {
                a = 1;
            } else if (window[i] < 0) {
                a = -1;
            } else {
                a = 0;
            }

            if (win[i] > 0) {
                b = 1;
            } else if (win[i] < 0) {
                b = -1;
            } else {
                b = 0;
            }

            sum = sum + Math.abs(a - b);
        }
        double zcr = sum * (1 / (2 * (double) length));
        return zcr;
    }
}
