package com.eatingdetection.gy.ihearfood.AudioFeatures;

import android.util.Log;

/**
 * Created by GY on 3/16/2016.
 */
public class SpectralCentroid {
    public static double[] featureSpectralCentroid(double []windowFFT, double fs){
        double eps = 2.220446049250313e-16;
        double [] CS = new double[2];
        double c1 = 0, c2 = 0;
        double max = maxValue(windowFFT);
        //System.out.println(Double.toString(max));
        //number of DFT coefs
        int windowLength = windowFFT.length, i;
        double [] m = new double[windowLength];
        //System.out.println("max = " + max);
        for(i=0; i<windowLength; i++){
            //sample range
            m[i] = i+1;
            m[i] = m[i] * ( fs / ( 2 * windowLength ) );
            //normalize the DFT coefs by the max value:

            windowFFT[i] = windowFFT[i] / max;
        }
        //System.out.println("m: " + m[0] + "\t" + m[windowLength-1]);

        //compute the spectral centroid:
        for(i=0; i<windowLength; i++){
            c1 = c1 + (m[i] * windowFFT[i]);
            c2 = c2 + windowFFT[i] ;
        }
        CS[0] = c1/(c2 + eps);
        //System.out.println(Double.toString(c2));
        c1 = 0.0;
        // compute the spectral spread
        for(i=0; i<windowLength; i++){
            c1 = c1 + (m[i]-CS[0]) * (m[i]-CS[0]) * windowFFT[i];
        }

        //System.out.println(Double.toString(c1));
        CS[1] = Math.sqrt(c1/(c2 + eps));

        CS[0] = CS[0] / (fs/2);
        CS[1] = CS[1] / (fs/2);
        return CS;
    }

    public static double maxValue(double[] array){
        double max = 0;
        for(int i = 0; i<array.length; i++){
            if(array[i] > max){
                max = array[i];
            }
        }
        return max;
    }
}