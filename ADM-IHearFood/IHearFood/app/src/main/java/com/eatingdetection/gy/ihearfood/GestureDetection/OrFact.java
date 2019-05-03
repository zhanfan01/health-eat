package com.eatingdetection.gy.ihearfood.GestureDetection;


import java.util.ArrayList;

public class OrFact {
  protected static final Float SCALE_NORMAL = (float) 1000;
  protected static final Float SCALE_SIGNAL = (float) 100;
  
  protected int mGravCount;
  protected Float mPitchMaximum;
  protected Float mPitchMinimum;
  protected Float mRollMaximum;
  protected Float mRollMinimum;
  protected Float mPitchMean;
  protected Float mPitchDeviation;
  protected Float mPitchJitter;
  protected Float mRollMean;
  protected Float mRollDeviation;
  protected Float mRollJitter;
  protected Float mPitchVariance;
  protected Float mRollVariance;
  protected int mPitchCount;
  protected int mRollCount;
  protected ArrayList<Float> mInputX = new ArrayList<Float>();
  protected ArrayList<Float> mInputY = new ArrayList<Float>();
  protected ArrayList<Float> mInputZ = new ArrayList<Float>();
  
  public OrFact() {
    clear();
  }
  
  public void clear() {
    mGravCount= 0;
    mPitchMaximum = Float.MIN_VALUE;
    mPitchMinimum = Float.MAX_VALUE;
    mRollMaximum = Float.MIN_VALUE;
    mRollMinimum = Float.MAX_VALUE;
    mPitchMean = (float) 0.0;
    mPitchDeviation = (float) 0;
    mPitchJitter = (float) 0;
    mPitchCount = 0;
    mRollMean = (float) 0.0;
    mRollDeviation = (float) 0;
    mRollJitter = (float) 0;
    mRollCount = 0;
  }
  
  public int getGravCount() {
    return mGravCount;
  }
  
  public Float getPitchMaximum() {
    return mPitchMaximum;
  }
  
  public Float getPitchMinimum() {
    return mPitchMinimum;
  }
  
  public Float getRollMaximum() {
    return mRollMaximum;
  }
  
  public Float getRollMinimum() {
    return mRollMinimum;
  }
  
  public Float getPitchMean() {
    return mPitchMean;
  }
  
  public Float getPitchDeviation() {
    return mPitchDeviation;
  }
  
  public Float getPitchVariance() {
    return mPitchVariance;
  }
  
  public Float getPitchJitter() {
    return mPitchJitter;
  }
  
  public Float getRollMean() {
    return mRollMean;
  }
  
  public Float getRollDeviation() {
    return mRollDeviation;
  }
  
  public Float getRollVariance() {
    return mRollVariance;
  }
  
  public Float getRollJitter() {
    return mRollJitter;
  }
  
  public void put(ArrayList<Float> pDataSetX, ArrayList<Float> pDataSetY, ArrayList<Float> pDataSetZ) {
  	mInputX = pDataSetX;
  	mInputY = pDataSetY;
  	mInputZ = pDataSetZ;
  	Descriptive(mInputX,mInputY,mInputZ);
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
  
  private void Descriptive(ArrayList<Float> xSet, ArrayList<Float> ySet, ArrayList<Float> zSet){
  	ArrayList<Float> pitchset = new ArrayList<Float>();
  	ArrayList<Float> rollset = new ArrayList<Float>();
  	for (int i=0; i<xSet.size();i++) {
  		if (isGravity(xSet.get(i),ySet.get(i),zSet.get(i))) {
  			mGravCount++;
  			pitchset.add(pitch(xSet.get(i),ySet.get(i),zSet.get(i)));
  			rollset.add(roll(xSet.get(i),ySet.get(i),zSet.get(i)));
  			DescriptivePitch(pitchset);
  			DescriptiveRoll(pitchset);
  		}
  	}
  }
  	  
	private boolean isGravity(float x, float y, float z){
		if(995 < Math.sqrt(x*x+y*y+z*z) && Math.sqrt(x*x+y*y+z*z) < 1005 ){
			return true;
		} else {
			return false;
	  }
  }
	
	private float roll(float x, float y, float z){
		
		float result = (float)  Math.toDegrees(Math.atan2(z,y));   // atan2(x,y)= y/x,we want y/z
		return result;
  }

	private float pitch(float x, float y, float z){
	
		float result = (float)  Math.toDegrees(Math.atan2(Math.sqrt(y*y+z*z),-x));   // atan2(x,y)= y/x,we want y/z
		return result;
	}
	
	private void DescriptivePitch(ArrayList<Float> pDataSet){
  	if((pDataSet != null) && (pDataSet.size() > 1)) {
      Float tPrevious = pDataSet.get(0);
      mPitchMinimum = pDataSet.get(0);
      mPitchMaximum = pDataSet.get(0);
      for(int i = 0; i < pDataSet.size(); ++i) {
        Float tValue = pDataSet.get(i);
        if(tValue < mPitchMinimum) {
          mPitchMinimum = tValue;
        }
        if(tValue > mPitchMaximum) {
          mPitchMaximum = tValue;
        }
        mPitchMean += tValue;
        mPitchDeviation += tValue * tValue;
        mPitchJitter += Math.abs(tValue - tPrevious);
        tPrevious = tValue;
        mPitchCount++;
      }
      if(mPitchCount > 0) {
        mPitchMean /= mPitchCount;
        mPitchDeviation /= mPitchCount;
        mPitchVariance = (mPitchDeviation - mPitchMean * mPitchMean)*mPitchCount/(mPitchCount-1); // unbiased 
        mPitchDeviation = (float) Math.sqrt(mPitchVariance);
        if(mPitchCount > 1) {
          mPitchJitter /= (mPitchCount - 1);
        }
      }
    }
  }
	
	private void DescriptiveRoll(ArrayList<Float> pDataSet){
  	if((pDataSet != null) && (pDataSet.size() > 1)) {
      Float tPrevious = pDataSet.get(0);
      mRollMinimum = pDataSet.get(0);
      mRollMaximum = pDataSet.get(0);
      for(int i = 0; i < pDataSet.size(); ++i) {
        Float tValue = pDataSet.get(i);
        if(tValue < mRollMinimum) {
          mRollMinimum = tValue;
        }
        if(tValue > mRollMaximum) {
          mRollMaximum = tValue;
        }
        mRollMean += tValue;
        mRollDeviation += tValue * tValue;
        mRollJitter += Math.abs(tValue - tPrevious);
        tPrevious = tValue;
        mRollCount++;
      }
      if(mRollCount > 0) {
        mRollMean /= mRollCount;
        mRollDeviation /= mRollCount;
        mRollVariance = (mRollDeviation - mRollMean * mRollMean)*mRollCount/(mRollCount-1); // unbiased 
        mRollDeviation = (float) Math.sqrt(mRollVariance);
        if(mRollCount > 1) {
          mRollJitter /= (mRollCount - 1);
        }
      }
    }
  }

  
}
