package com.eatingdetection.gy.ihearfood.AudioFeatures;

/**
 * Created by GY on 3/16/2016.
 */
public class Chroma {
    public static double[] getChroma(double[] window, double Fs) {
        int winLength = window.length;

        double[] nwindow = new double[winLength];

        double tone_analysis = 12;
        int num_of_bins = 12;

        double max = 0;
        double x;
        for (int i = 0; i < winLength; i++) {
            x = Math.abs(window[i]);
            if (x > max) {
                max = x;
            }
        }
        for (int i = 0; i < winLength; i++) {
            nwindow[i] = window[i] / max;
        }

        //System.out.println("fftMag");

        // double[] fft = FFTbase.fft(window, iwindow, true);
        double[] fft = FFT.fft(nwindow);

        int fLength = (int) Math.floor(winLength / 2);

        double[] fftMag = new double[fLength];
        double the_max = 0;
        for (int i = 0; i < fLength; i++) {
            fftMag[i] = fft[i];
            if (fftMag[i] > the_max) {
                the_max = fftMag[i];
            }
        }

        //System.out.println("freqs");

        double f0 = 55;
        double step = Fs / winLength;
        int length = (int) ((Math.floor(winLength / 2) - 1) * (Fs / winLength)
                / step + 1);
        double[] freqs = new double[length];
        for (int i = 0; i < length; i++) {
            freqs[i] = i * step;
            if (freqs[i] < f0 || freqs[i] > 2000) {
                fftMag[i] = 0;
            }
        }
        double last_freqs = freqs[length - 1];

        //System.out.println("f");

        int last_index = (int) Math.ceil(Math.log(last_freqs / f0)
                / Math.log(2) * tone_analysis);
        double[] f = new double[last_index];
        for (int i = 0; i < last_index; i++) {
            f[i] = f0 * Math.pow(2, (i / tone_analysis));
        }

        //System.out.println("c1");
        //System.out.println("c2");

        double[] c1 = new double[fLength];
        double[] c2 = new double[fLength];
        int count = 0;
        int count2 = 0;

        for (int i = 0; i < fLength; i++) {
            if (i == 0) {
                c1[i] = fftMag[i] - 0;
            } else {
                c1[i] = fftMag[i] - fftMag[i - 1];
            }
            if (i == (fLength - 1)) {
                c2[i] = fftMag[i] - 0;
            } else {
                c2[i] = fftMag[i] - fftMag[i + 1];
            }
        }
        for (int i = 0; i < fLength; i++) {
            if (!(c1[i] > 0 && c2[i] > 0)) {
                fftMag[i] = 0;
                count2++;
            }
            if (fftMag[i] > 0) {
                count++;
            }
        }

        //System.out.println("count2 " + count2);
        //System.out.println("count " + count);

        int[] nonzero = new int[count];
        for (int i = 0, j = 0; i < fLength; i++) {
            if (fftMag[i] > 0) {
                nonzero[j] = i;
                j++;
            }
        }

        double[] ytemp = new double[num_of_bins];
        double[] ctemp = new double[num_of_bins];

        double temp, min, s;
        int MIN_index, h;
        // System.out.println("nonzero.length " + nonzero.length);
        // System.out.println("freqs.length " + freqs.length);
        for (int k = 0; k < nonzero.length; k++) {
            //System.out.println("nonzero[" + k + "] " + nonzero[k]);
            temp = freqs[nonzero[k]];
            //System.out.println("temp " + temp);
            min = 9999999;
            MIN_index = 0;
            for (int i = 0; i < last_index; i++) {
                s = Math.abs(temp - f[i]);
                if (s < min) {
                    min = s;
                    MIN_index = i;
                }
            }
            h = MIN_index % num_of_bins;

            ytemp[h] = ytemp[h] + fftMag[nonzero[k]];
            ctemp[h] = ctemp[h] + 1;
        }

        //System.out.println("ytemp");
        //printArray(ytemp);
        //System.out.println("ctemp");
        //printArray(ctemp);
        //System.out.println("chroma");

        double[] chroma = new double[num_of_bins];
        for (int i = 0; i < num_of_bins; i++) {
            chroma[i] = ytemp[i] / (ctemp[i] + 1);
        }
        return chroma;
    }
}
