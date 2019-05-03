package com.eatingdetection.gy.ihearfood.GestureDetection;

import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.BFTree;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Arff {

    private Instances mInstances;
    private FastVector mAttClass;

    /**
     * Constructor
     */
    public Arff() {

        FastVector atts;
        int i;

        // 1. set up attributes
        atts = new FastVector();
        // - numeric
        atts.addElement(new Attribute("x1"));
        atts.addElement(new Attribute("x2"));
        atts.addElement(new Attribute("x3"));
        atts.addElement(new Attribute("x4"));
        atts.addElement(new Attribute("x5"));
        atts.addElement(new Attribute("x6"));
        atts.addElement(new Attribute("y1"));
        atts.addElement(new Attribute("y2"));
        atts.addElement(new Attribute("y3"));
        atts.addElement(new Attribute("y4"));
        atts.addElement(new Attribute("y5"));
        atts.addElement(new Attribute("y6"));
        atts.addElement(new Attribute("z1"));
        atts.addElement(new Attribute("z2"));
        atts.addElement(new Attribute("z3"));
        atts.addElement(new Attribute("z4"));
        atts.addElement(new Attribute("z5"));
        atts.addElement(new Attribute("z6"));
        atts.addElement(new Attribute("or0"));
        atts.addElement(new Attribute("or1"));
        atts.addElement(new Attribute("or2"));
        atts.addElement(new Attribute("or3"));
        atts.addElement(new Attribute("or4"));
        atts.addElement(new Attribute("or5"));
        atts.addElement(new Attribute("or6"));
        atts.addElement(new Attribute("or7"));
        atts.addElement(new Attribute("or8"));
//     atts.addElement(new Attribute("or9"));
//     atts.addElement(new Attribute("or10"));
//     atts.addElement(new Attribute("or11"));
//     atts.addElement(new Attribute("or12"));
//     atts.addElement(new Attribute("ftty4"));
//     atts.addElement(new Attribute("ftty5"));
//     atts.addElement(new Attribute("ftty6"));
//     atts.addElement(new Attribute("ftty7"));
//     atts.addElement(new Attribute("ftty8"));
//     atts.addElement(new Attribute("ftty9"));
//     atts.addElement(new Attribute("fttz0"));
//     atts.addElement(new Attribute("fttz1"));
//     atts.addElement(new Attribute("fttz2"));
//     atts.addElement(new Attribute("fttz3"));
//     atts.addElement(new Attribute("fttz4"));
//     atts.addElement(new Attribute("fttz5"));
//     atts.addElement(new Attribute("fttz6"));
//     atts.addElement(new Attribute("fttz7"));
//     atts.addElement(new Attribute("fttz8"));
//     atts.addElement(new Attribute("fttz9"));

        // - nominal
        mAttClass = new FastVector();
        mAttClass.addElement("eating");
//     mAttClass.addElement("eating");
        mAttClass.addElement("non-eating");
        atts.addElement(new Attribute("att7", mAttClass));

        // 2. create Instances object
        mInstances = new Instances("MyRelation", atts, 0);
        mInstances.setClassIndex(mInstances.numAttributes() - 1);
    }


    public void addInstance(ArrayList<Float> tDataSet) throws Exception {

        double[] vals;
        // 3. fill with data
        // first instance
        vals = new double[mInstances.numAttributes()];
        for (int i = 0; i < mInstances.numAttributes() - 1; i++) {
            vals[i] = tDataSet.get(i);
        }
        vals[mInstances.numAttributes() - 1] = 0;
        mInstances.add(new Instance(1.0, vals));

    }

    public void printInstances() {
        System.out.println(mInstances);
    }

    public Instances getInstances() {
        return mInstances;
    }

    public Instance getInstance(int tIndex) {
        return mInstances.instance(tIndex);
    }


}