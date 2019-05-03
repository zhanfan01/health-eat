package com.eatingdetection.gy.ihearfood.GestureDetection;


import java.util.ArrayList;

public class Fact {
  protected static final Float SCALE_NORMAL = (float) 1000;
  protected static final Float SCALE_SIGNAL = (float) 100;
  
  protected Float mMinimum;
  protected Float mMaximum;
  protected Float mMean;
  protected Float mDeviation;
  protected Float mJitter;
  protected Float mMaxMin;
  protected Float mVariance;
  protected Float mMAV;
  protected int mCount;
  protected ArrayList<Float> mInput = new ArrayList<Float>();
  
  public Fact() {
    clear();
  }
  
  public void clear() {
    mMinimum = Float.MAX_VALUE;
    mMaximum = Float.MIN_VALUE;
    mMean = (float) 0.0;
    mDeviation = (float) 0;
    mJitter = (float) 0;
    mCount = 0;
  }
  
  public Float getMinimum() {
    return mMinimum;
  }
  
  public Float getMaximum() {
    return mMaximum;
  }
  
  public Float getMean() {
    return mMean;
  }
  
  public Float getDeviation() {
    return mDeviation;
  }
  
  public Float getVariance() {
    return mVariance;
  }
  
  public Float getJitter() {
    return mJitter;
  }
  
  public Float getMaxMin() {
    return mMaximum - mMinimum;
  }
  
  public Float getMAV(){
  	Float tValue = 0f;
  	int tCount = 0;
  	if((mInput != null) && (mInput.size() > 1)) {
      for(int i = 0; i < mInput.size(); ++i) {
        tValue = tValue + Math.abs(mInput.get(i));
        tCount++;
      }
    }
  	tValue = tValue / tCount;
  	return tValue;
  }
  
  public Float getRMS(){
  	Float tValue = 0f;
  	int tCount = 0;
  	if((mInput != null) && (mInput.size() > 1)) {
      for(int i = 0; i < mInput.size(); ++i) {
        tValue = tValue + (float) Math.pow((mInput.get(i)),2);
        tCount++;
      }
    }
  	tValue = (float) Math.sqrt(tValue / tCount);
  	return tValue;
  }
  
  public int getNoZC(){
  	Float tValue = 0f;
  	int tCount = 0;
  	if((mInput != null) && (mInput.size() > 2)) {
  		tValue =  mInput.get(0);
      for(int i = 1; i < mInput.size(); ++i) {
      	if ((tValue < 0 &&  mInput.get(i) >0) || (tValue > 0 &&  mInput.get(i) < 0)){
        tCount++;
      	}
      	tValue =  mInput.get(i);
      }
    }
  	return tCount;
  }
  
  public int getNoPeak(){
  	Float tValue = 0f;
  	int tCount = 0;
  	if((mInput != null) && (mInput.size() > 2)) {
  		tValue =  mInput.get(0);
      for(int i = 1; i < mInput.size()-1; ++i) {
      	if ((tValue < mInput.get(i) &&  mInput.get(i) > mInput.get(i+1))){
        tCount++;
      	}
      	tValue =  mInput.get(i);
      }
    }
  	return tCount;
  }
  
  public Float getMeanPeak(){
  	Float tValue = 0f;
  	Float tSum = 0f;
  	int tCount = 0;
  	if((mInput != null) && (mInput.size() > 2)) {
  		tValue =  mInput.get(0);
      for(int i = 1; i < mInput.size()-1; ++i) {
      	if ((tValue < mInput.get(i) &&  mInput.get(i) > mInput.get(i+1))){
      		tSum = tSum + mInput.get(i);
      		tCount++;
      	}
      	tValue =  mInput.get(i);
      }
    }
  	return tSum/tCount;
  }
  
  
  public int getSize() {
    return mCount;
  }
  
  public void put(ArrayList<Float> pDataSet) {
  	mInput = pDataSet;
  	Descriptive(mInput);
  }
  
  public static Float roundToFloat(Float pValue) {
    return round(pValue, SCALE_NORMAL);
  }
  
  private static Float round(Float pValue, Float pScale) {
    return Math.round(pValue * pScale) / pScale;
  }
  
  public static long roundToLong(Float pValue) {
    return Math.round(pValue);
  }
  
  
  public String toString() {
    return (mCount + " " + mMinimum + " " + mMean + " " + mMaximum
        + " " + mDeviation);
  }
  
  private void Descriptive(ArrayList<Float> pDataSet){
  	if((pDataSet != null) && (pDataSet.size() > 1)) {
      Float tPrevious = pDataSet.get(0);
      mMinimum = pDataSet.get(0);
      mMaximum = pDataSet.get(0);
      for(int i = 0; i < pDataSet.size(); ++i) {
        Float tValue = pDataSet.get(i);
        if(tValue < mMinimum) {
          mMinimum = tValue;
        }
        if(tValue > mMaximum) {
          mMaximum = tValue;
        }
        mMean += tValue;
        mDeviation += tValue * tValue;
        mJitter += Math.abs(tValue - tPrevious);
        tPrevious = tValue;
        mCount++;
      }
      if(mCount > 0) {
        mMean /= mCount;
        mDeviation /= mCount;
        mVariance = (mDeviation - mMean * mMean)*mCount/(mCount-1); // unbiased 
        mDeviation = (float) Math.sqrt(mVariance);
        if(mCount > 1) {
          mJitter /= (mCount - 1);
        }
      }
    }
  }

  
  
}
