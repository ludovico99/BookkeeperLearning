package org.learning.bookkeeperLearning;

import org.jfree.chart.ChartUtilities;
import org.jfree.ui.RefineryUtilities;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Validation {

    private static final String FILE_NAME = "Bookkeeper";
    //Bookkeeper
    //Storm

    protected List<AbstractClassifier> classifiers;
    protected final String dataSetName ;

    protected DataSource trainingSet;
    protected DataSource testingSet;

    protected int iterations;

    protected List<Instances> trainings;
    protected List<Instances> testings;

    protected Validation( DataSource training, DataSource testing,String dataSetName) {
        testingSet = testing;
        trainingSet = training;


        this.dataSetName = dataSetName;

        this.trainings = new ArrayList<>();
        this.testings = new ArrayList<>();

        initClassifiers();

    }


    public int getIterations() {
        return iterations;
    }

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

    public abstract List<LearningModelEntity> validation() throws Exception;

    public void createInstances() throws Exception {

        iterations = trainingSet.getDataSet(0).numClasses();
        int numAttr = 1;
        Instances dataSet1 = trainingSet.getDataSet(0);
        Instances dataSet2 = testingSet.getDataSet(0);

        Instances training;
        Instances testing;

        for (double i = 1.0; i < iterations; i++) {
            training = new Instances(dataSet1, 0, 0);
            testing = new Instances(dataSet2, 0, 0);
            for (Instance in : dataSet1) {
                if (in.classValue() < i) training.add(in);
            }
            for (Instance in : dataSet2) {
                if (in.classValue() == i) testing.add(in);
            }
            if (training.isEmpty() || testing.isEmpty()) continue;

            numAttr = training.numAttributes();

            training.setClassIndex(numAttr - 1);
            testing.setClassIndex(numAttr - 1);

            addTesting(testing);
            addTraining(training);


        }
    }


    public  BoxChart showChart(List<LearningModelEntity> entities,MetricsEnum e){

        final String title = "Which classifier is the best one?";
        BoxChart chart  = new BoxChart(dataSetName,title,e,entities);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
        return chart;
    }

    public void initClassifiers() {
        classifiers = new ArrayList<>();

        classifiers.add(new NaiveBayes());
        classifiers.add(new RandomForest());
        classifiers.add(new IBk());
    }
    public List<LearningModelEntity> initLearningModelEntities () {
        List<LearningModelEntity> results = new ArrayList<>();
        for (AbstractClassifier classifier : classifiers) {
            String[] tokenizedStr = classifier.getClass().toString().split("\\.");
            String classifierName = tokenizedStr[tokenizedStr.length - 1];

            LearningModelEntity learningModelEntity = new LearningModelEntity(classifierName, dataSetName, iterations - 1);

            learningModelEntity.setTestings(testings);
            learningModelEntity.setTrainings(trainings);

            results.add(learningModelEntity);
        }
        return results;
    }

    public void addClassifiers(AbstractClassifier classifier){
        classifiers.add(classifier);
    }

    public void saveChart(BoxChart chart, String str) throws IOException {
        File boxChart = new File( FILE_NAME + "_BoxChart_" + str + ".jpeg" );
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        ChartUtilities.saveChartAsJPEG(boxChart ,chart.getChart(), dim.width ,dim.height);
    }

    public abstract Evaluation buildModel(AbstractClassifier classifier, Instances trainings, Instances testings,LearningModelEntity modelEntity) throws Exception;
}

