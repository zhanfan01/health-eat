package com.eatingdetection.gy.ihearfood.AudioFeatures;

/**
 * Created by GY on 3/16/2016.
 */
public class SpectralRolloff {
    public static double featureSpectralRolloff(double []windowFFT, double c){
        double mc = 0.0;
        double curEnergy = 0.0;
        double totalEnergy = 0.0;
        int countFFT = 0;
        double fftLength = windowFFT.length;

        //compute total spectral energy:
        for(int i=0; i<fftLength; i++){
            totalEnergy = totalEnergy + windowFFT[i] * windowFFT[i];
        }

        // find the spectral rolloff as the frequency position where the
        // respective spectral energy is equal to c*totalEnergy
        while((curEnergy <= c * totalEnergy) && (countFFT < fftLength)){
            curEnergy = curEnergy + windowFFT[countFFT] * windowFFT[countFFT];
            countFFT++;
        }
        //countFFT--;
        //System.out.println("countFFT= " + countFFT + "\tfftLength= " + fftLength);
        mc = (countFFT-1)/fftLength;
        //System.out.println("mc= " + mc);
        return mc;
    }
}

