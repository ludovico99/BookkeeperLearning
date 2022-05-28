package org.learning.bookkeeperlearning.entity;

import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class LearningModelEntity {

    private String classifier;
    private String dataSetName;

    private List<Instances> trainings;
    private List<Instances> testings;

    private int iterations;

    private final List<Double> tp;
    private final List<Double> tn;
    private final List<Double> fp;
    private final List<Double> fn;

    private final List<Double> accuracy;
    private final List<Double> recall;
    private final List<Double> precision;
    private final List<Double> kappa;
    private final List<Double> rocAuc;

    private String balancing = "";
    private boolean featureSelection = false;

    @Override
    public String toString() {
        return "LearningModelEntity{" +
                "classifier='" + classifier + '\'' +
                ", dataSetName='" + dataSetName + '\'' +
                ", training=" + trainings +
                ", testing=" + testings +
                ", iterations=" + iterations +
                ", tp=" + tp +
                ", tn=" + tn +
                ", fp=" + fp +
                ", fn=" + fn +
                ", accuracy=" + accuracy +
                ", recall=" + recall +
                ", precision=" + precision +
                ", kappa=" + kappa +
                ", rocAuc=" + rocAuc +
                ", balancing='" + balancing + '\'' +
                ", featureSelection=" + featureSelection +
                '}';
    }

    public void setFeatureSelection(boolean featureSelection) {
        this.featureSelection = featureSelection;
    }

    public boolean isFeatureSelection() {
        return featureSelection;
    }

    public LearningModelEntity(String classifier,String dataSetName, int iterations) {
        this.classifier = classifier;
        this.dataSetName = dataSetName;
        this.iterations = iterations;
        trainings = new ArrayList<>();
        testings = new ArrayList<>();
        tp = new ArrayList<>();
        tn = new ArrayList<>();
        fp = new ArrayList<>();
        fn = new ArrayList<>();
        accuracy = new ArrayList<>();
        recall = new ArrayList<>();
        precision = new ArrayList<>();
        kappa = new ArrayList<>();
        rocAuc = new ArrayList<>();

    }

    public List<Double> computeMeanValues(){
        double meanAccuracy = 0;
        double meanRecall = 0;
        double meanPrecision = 0;
        double meanRocAuc = 0;
        double meanKappa = 0;
        for (int i= 0;i< iterations;i++){
            meanAccuracy = meanAccuracy + accuracy.get(i);
            meanRecall = meanRecall + recall.get(i);
            meanPrecision = meanPrecision + precision.get(i);
            meanRocAuc = meanRocAuc + rocAuc.get(i);
            meanKappa = meanKappa + kappa.get(i);
        }
        List<Double> result = new ArrayList<>();
        result.add(meanAccuracy/iterations);
        result.add(meanRecall/iterations);
        result.add(meanPrecision/iterations);
        result.add(meanRocAuc/iterations);
        result.add(meanKappa/iterations);
        return result;
    }

    public String getBalancing() {
        return balancing;
    }

    public void setBalancing(String balancing) {
        this.balancing = balancing;
    }


    public int getIterations() {
        return iterations;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public void setDataSetName(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public List<Instances> getTrainings() {
        return trainings;
    }


    public List<Instances> getTestings() {
        return testings;
    }

    public void setTestings(List<Instances> testings) {
        this.testings = testings;
    }

    public void setTrainings(List<Instances> trainings) {
        this.trainings = trainings;
    }

    public List<Double> getTp() {
        return tp;
    }

    public void addTp(Double tp) {
        this.tp.add(tp);
    }

    public List<Double> getTn() {
        return tn;
    }

    public void addTn(Double tn) {
        this.tn.add(tn);
    }

    public List<Double> getFp() {
        return fp;
    }

    public void addFp(Double fp) {
        this.fp.add(fp);
    }

    public List<Double> getFn() {
        return fn;
    }

    public void addFn(Double fn) {
        this.fn.add(fn);
    }

    public List<Double> getAccuracy() {
        return accuracy;
    }

    public void addAccuracy(Double accuracy) {
        this.accuracy.add(accuracy);
    }

    public List<Double> getRecall() {
        return recall;
    }

    public void addRecall(Double recall) {
        this.recall.add(recall);
    }

    public List<Double> getPrecision() {
        return precision;
    }

    public void addPrecision(Double precision) {
        this.precision.add(precision);
    }

    public List<Double> getKappa() {
        return kappa;
    }

    public void addKappa(Double kappa) {
        this.kappa.add(kappa);
    }

    public List<Double> getRocAuc() {
        return rocAuc;
    }

    public void addRocAuc(Double rocAuc) {
        this.rocAuc.add(rocAuc);
    }
}
