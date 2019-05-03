package com.eatingdetection.gy.realtimeeating.AudioFeatures;

import org.jtransforms.fft.DoubleFFT_1D;

public class FFT {
    public static double[] fft(double[] window){
        int length = window.length;
        double[] fft = new double[length];
        double[] complexfft = new double[length * 2];

        DoubleFFT_1D fftDo = new DoubleFFT_1D(length);
        System.arraycopy(window, 0, complexfft, 0, length);
        fftDo.realForwardFull(complexfft);

        for(int i=0; i<length; i++) {
            //System.out.println(i + ": " + fft[i]);
            fft[i] = Math.hypot(complexfft[2*i], complexfft[2*i+1]);
        }
        return fft;
    }
}
