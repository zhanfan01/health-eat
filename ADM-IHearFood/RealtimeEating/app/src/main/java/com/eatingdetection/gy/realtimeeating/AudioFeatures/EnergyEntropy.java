package com.eatingdetection.gy.realtimeeating.AudioFeatures;

/**
 * Created by GY on 3/15/2016.
 */

public class EnergyEntropy {
    public static double getEntropy(double[] window, int numOfShortBlocks) {
        double Eol = 0;
        double[] subEnergies;
        double sum = 0;

        int winLength = window.length;
        int subWinLength = (int) Math.floor(winLength / numOfShortBlocks);
        int length = subWinLength * numOfShortBlocks;

        // total frame energy
        for (int i = 0; i < winLength; i++) {
            Eol = Eol + Math.abs(window[i] * window[i]);
        }

        if (winLength != length) {
            double[] new_window = java.util.Arrays.copyOfRange(window, 0,
                    length);
            subEnergies = subFrameEnergies(new_window, subWinLength,
                    numOfShortBlocks, Eol);
        } else {
            subEnergies = subFrameEnergies(window, subWinLength,
                    numOfShortBlocks, Eol);
        }

        // compute entropy of the normalized sub-frame energies
        for (int i = 0; i < numOfShortBlocks; i++) {
            sum = sum
                    + subEnergies[i]
                    * (Math.log(subEnergies[i] + Math.pow(2, -52)) / Math
                    .log(2));
        }

        double Entropy = -sum;

        return Entropy;
    }

    // compute normalized sub-frame energies
    public static double[] subFrameEnergies(double[] window, int subWinLength,
                                            int numOfShortBlocks, double Eol) {
        double[] subEnergies = new double[numOfShortBlocks];
        double sum;
        int index = 0;
        for (int i = 0; i < numOfShortBlocks; i++) {
            sum = 0;
            for (int j = 0; j < subWinLength; j++) {
                sum = sum + Math.abs(window[index + j] * window[index + j]);
            }
            subEnergies[i] = sum / (Eol + Math.pow(2, -52));
            index = index + subWinLength;
        }
        return subEnergies;
    }
}
