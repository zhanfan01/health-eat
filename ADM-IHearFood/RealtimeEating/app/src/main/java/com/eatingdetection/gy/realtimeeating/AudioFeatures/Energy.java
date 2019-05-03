package com.eatingdetection.gy.realtimeeating.AudioFeatures;

/**
 * Created by GY on 3/15/2016.
 */

public class Energy {
    public static double getEnergy(double[] window) {
        int winLength = window.length;
        double sum = 0;
        for(int i = 0; i < winLength; i++){
            sum = sum + Math.pow(window[i], 2);
        }
        return sum / winLength;
    }
}
