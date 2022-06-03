package org.learning.bookkeeperlearning.entity;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;

public class ValidationEntity {
    protected List<AbstractClassifier> classifiers;
    protected final String dataSetName ;

    protected DataSource trainingSet;
    protected DataSource testingSet;


    public ValidationEntity(DataSource training, DataSource testing, String dataSetName) {
        testingSet = testing;
        trainingSet = training;


        this.dataSetName = dataSetName;

        this.trainings = new ArrayList<>();
        this.testings = new ArrayList<>();

        this.classifiers = new ArrayList<>();

        this.addClassifier(new NaiveBayes());
        this.addClassifier(new RandomForest());
        this.addClassifier(new IBk());
        this.addClassifier(new J48());

    }


    protected List<Instances> trainings;
    protected List<Instances> testings;


    public List<Instances> getTestings() {
        return testings;
    }

    public void addTraining(Instances instances) {
        this.trainings.add(instances);
    }

    public void addTesting(Instances instances) {
        this.testings.add(instances);
    }

    public List<Instances> getTrainings() {
        return trainings;
    }

    public DataSource getTestingSet() {
        return testingSet;
    }

    public DataSource getTrainingSet() {
        return trainingSet;
    }

    public void setTestingSet(DataSource testingSet) {
        this.testingSet = testingSet;
    }

    public void setTrainingSet(DataSource trainingSet) {
        this.trainingSet = trainingSet;
    }

    public void addClassifier(AbstractClassifier classifier){
        classifiers.add(classifier);
    }

    public void setTrainings(List<Instances> trainings) {
        this.trainings = trainings;
    }

    public void setTestings(List<Instances> testings) {
        this.testings = testings;
    }

    public String getDataSetName() {
        return dataSetName;
    }

    public List<AbstractClassifier> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(List<AbstractClassifier> classifiers) {
        this.classifiers = classifiers;
    }

}
