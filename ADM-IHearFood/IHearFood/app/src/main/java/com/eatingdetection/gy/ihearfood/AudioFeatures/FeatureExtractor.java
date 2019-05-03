package com.eatingdetection.gy.ihearfood.AudioFeatures;

/**
 * Created by GY on 3/16/2016.
 */

import android.util.Log;

import java.nio.ByteBuffer;

public class FeatureExtractor {
    private static final int NUMBER_OF_BLOCKS = 100;
    private static final boolean DIRECT = true;
    private static final double SPECTRAL_ROLLOFF_PARAMETER = 0.95;
    private static final double MEAN = 11.481726202874848;
    private static final double STD = 12.422191839188253;
    private static final String TAG = "FeatureExtractor";

    public static double[] getFeatures(byte[] buffer, int SampleRate){
        double[] features = new double[18];
        String f = "";
        String rf = "";

        double[] window = WaveRead.GetWave(buffer);
        Log.d(TAG, "Window Length: " + window.length);

        features[0] = ZeroCrossingRate.ZCR(window);
        features[1] = Energy.getEnergy(window);
        features[2] = EnergyEntropy.getEntropy(window, NUMBER_OF_BLOCKS);
        // Log.d(TAG, "Zcr, Energy,Entropy done");

        //double[] fft = ComplexFFT.FFT(buffer);
        double[] fft = FFT.fft(window);
        int fftLength = fft.length;
        for(int i = 0; i < fftLength; i++){
            fft[i] = fft[i] / fftLength;
        }
        double[] halfFFT = new double[(int) Math.ceil(fftLength/2)];
        System.arraycopy(fft, 0, halfFFT, 0, fftLength/2);
        // Log.d(TAG, "FFT done");

        double[] centroid_features = SpectralCentroid.featureSpectralCentroid(halfFFT, SampleRate);
        features[3] = centroid_features[0];
        features[4] = centroid_features[1];
        features[5] = SpectralRolloff.featureSpectralRolloff(halfFFT, SPECTRAL_ROLLOFF_PARAMETER);
        // Log.d(TAG, "SpectralCentroid, SpectralRolloff done");

        double[] chroma = Chroma.getChroma(window, SampleRate);
        for(int i = 0; i < chroma.length; i++){
            features[6 + i] = chroma[i];
        }
        // Log.d(TAG, "Chroma done");

        for(int i = 0; i < 18; i++){
            rf = rf + Double.toString(features[i]) + ",";
            features[i] = (features[i] - MEAN) / STD;
            f = f + Double.toString(features[i]) + ",";
        }
        //Log.d(TAG, f);

        return features;
    }

}
