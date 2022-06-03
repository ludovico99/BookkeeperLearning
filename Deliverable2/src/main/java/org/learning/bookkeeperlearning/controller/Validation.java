package org.learning.bookkeeperlearning.controller;

import org.jfree.chart.ChartUtilities;
import org.jfree.ui.RefineryUtilities;
import org.learning.bookkeeperlearning.entity.LearningModelEntity;
import org.learning.bookkeeperlearning.utility.BoxChart;
import org.learning.bookkeeperlearning.utility.MetricsEnum;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Validation {

    private static final String FILE_NAME = ".\\Deliverable2\\src\\main\\resources\\Bookkeeper";
    private final Logger logger = Logger.getLogger("Validation controller log");


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

        this.classifiers = new ArrayList<>();

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

    public List<LearningModelEntity> validation() {

        createInstances();

        List<LearningModelEntity> results = initLearningModelEntities();

        for (int i = 0; i< trainings.size(); i++) {
            for (int j = 0; j < classifiers.size(); j++) {

                LearningModelEntity learningModelEntity = results.get(j);

                Evaluation eval = this.buildModel(classifiers.get(j),trainings.get(i),testings.get(i),learningModelEntity);


                if (eval != null) {
                    double recall = Math.round(eval.recall(1) * 100.0) / 100.0;
                    double precision = Math.round(eval.precision(1) * 100.0) / 100.0;
                    double accuracy = Math.round(eval.pctCorrect() * 100.0) / 100.0;
                    double auc = Math.round(eval.areaUnderROC(1) * 100.0) / 100.0;
                    double kappa = Math.round(eval.kappa() * 100.0) / 100.0;


                    learningModelEntity.addTp(eval.numTruePositives(1));
                    learningModelEntity.addTn(eval.numTrueNegatives(1));
                    learningModelEntity.addFp(eval.numFalsePositives(1));
                    learningModelEntity.addFn(eval.numFalseNegatives(1));

                    learningModelEntity.addAccuracy(accuracy);
                    learningModelEntity.addRecall(recall);
                    learningModelEntity.addPrecision(precision);
                    learningModelEntity.addKappa(kappa);
                    learningModelEntity.addRocAuc(auc);
                }
            }
        }
        return results;
    }

    public void createInstances() {

        try {

            iterations = trainingSet.getDataSet(0).numClasses();
            int numAttr;
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

                training.deleteAttributeAt(0);
                training.deleteAttributeAt(0);

                testing.deleteAttributeAt(0);
                testing.deleteAttributeAt(0);

                addTesting(testing);
                addTraining(training);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in retrieving data set" ,e);
        }

    }


    public BoxChart showChart(List<LearningModelEntity> entities, MetricsEnum e){

        final String title = "Which classifier is the best one?";
        BoxChart chart  = new BoxChart(dataSetName,title,e,entities);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
        return chart;
    }

    public void initClassifiers() {

        this.addClassifier(new NaiveBayes());
        this.addClassifier(new RandomForest());
        this.addClassifier(new IBk());

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

    public void addClassifier(AbstractClassifier classifier){
        classifiers.add(classifier);
    }

    public void saveChart(BoxChart chart, String str)   {
        try {
            File boxChart = new File(FILE_NAME + "_BoxChart_" + str + ".jpeg");
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            ChartUtilities.saveChartAsJPEG(boxChart, chart.getChart(), dim.width, dim.height);
        } catch(IOException e){
            logger.log(Level.SEVERE,"Error in saving box chart as Jpeg" ,e);
        }
    }

    public abstract Evaluation buildModel(AbstractClassifier classifier, Instances training, Instances testing, LearningModelEntity modelEntity);
}

