package com.eatingdetection.gy.ihearfood.GestureDetection;

import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;


/**
 * Copyright (C) 2015  Xu Ye
 */

public class DEThreshold {

	private ArrayList<Float> mBuffer = new ArrayList<Float>();
	private ArrayList<Float> mMagnitude = new ArrayList<Float>();
	private int mDetected;
	private Classifier mClassifier;
	private ArrayList<Float> mX = new ArrayList<Float>();
	private ArrayList<Float> mY = new ArrayList<Float>();
	private ArrayList<Float> mZ = new ArrayList<Float>();
	private ArrayList<Float> mInstance = new ArrayList<Float>();

  /**
   * Constructor
   */
  public DEThreshold(ArrayList<Float> buffer, Classifier model) {
  	mBuffer = buffer; // buffer stores x,y,z consecutively
  	mDetected = 0;
  	mClassifier = model;
  	Compare();
  }


  public int getDetected(){
  	return mDetected;
  }
  
  public void getMagnitude(){
  	for(int i=0; i<mBuffer.size()/3;i++){
  		mMagnitude.add((float) Math.sqrt(Math.pow(mBuffer.get(i*3),2)+Math.pow(mBuffer.get(i*3+1),2)
  				+Math.pow(mBuffer.get(i*3+2),2)));
  	}
  }
  
  public void Compare(){
  	System.out.println("Jin le compare()");
  	Arff mArff = new Arff();
  	for(int j=0;j<mBuffer.size()/3;j++){
	    mX.add(mBuffer.get(j*3));
	    mY.add(mBuffer.get(j*3+1));
	    mZ.add(mBuffer.get(j*3+2));
  	}
  	mInstance = buildInstance(mX,mY,mZ);
		try {
			mArff.addInstance(mInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Evaluation eTest = new Evaluation(mArff.getInstances());
			double pred = mClassifier.classifyInstance(mArff.getInstance(0));
			System.out.println("Prediction = " +pred);
			if ( pred==0 ) {
				mDetected = 1;
				System.out.println("jiancedaole");;
			}
		} catch (Exception e) {
			System.out.println("Classification Error!!!!");
			e.printStackTrace();
		}
  	
  }
  
  private ArrayList<Float> buildInstance(ArrayList<Float> tX,ArrayList<Float> tY,
			 ArrayList<Float> tZ){
		 ArrayList<Float> Instance = new ArrayList<Float>();
		 Fact xFact = new Fact();
	   Fact yFact = new Fact();
	   Fact zFact = new Fact();
	   OrFact orFact = new OrFact();
	   xFact.put(tX);
	   yFact.put(tY);
	   zFact.put(tZ);
	   orFact.put(tX,tY,tZ);
	   
	   Instance.add(xFact.getMean());
	   Instance.add(yFact.getMean());
	   Instance.add(zFact.getMean());
	    
	   Instance.add(xFact.getVariance());
	   Instance.add(yFact.getVariance());
	   Instance.add(zFact.getVariance());
	    
	   Instance.add(xFact.getMaximum());
	   Instance.add(yFact.getMaximum());
	   Instance.add(zFact.getMaximum());
	    
	   Instance.add(xFact.getMinimum());
	   Instance.add(yFact.getMinimum());
	   Instance.add(zFact.getMinimum());
	    
	   Instance.add(xFact.getJitter());
	   Instance.add(yFact.getJitter());
	   Instance.add(zFact.getJitter());
	    
	   Instance.add(xFact.getMaxMin());
	   Instance.add(yFact.getMaxMin());
	   Instance.add(zFact.getMaxMin());
	   
	   Instance.add((float) orFact.getGravCount());
	   Instance.add(orFact.getPitchMaximum());
	   Instance.add(orFact.getPitchMinimum());
	   Instance.add(orFact.getPitchMean());
	   Instance.add(orFact.getPitchDeviation());
//	   Instance.add(orFact.getPitchVariance());
//	   Instance.add(orFact.getPitchJitter());
	   Instance.add(orFact.getRollMaximum());
	   Instance.add(orFact.getRollMinimum());
	   Instance.add(orFact.getRollMean());
	   Instance.add(orFact.getRollDeviation());
//	   Instance.add(orFact.getRollVariance());
//	   Instance.add(orFact.getRollJitter());
	   
	   return Instance;
  }
	
	
}
