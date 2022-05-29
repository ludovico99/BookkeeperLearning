package org.learning.bookkeeperlearning.controller;

import org.learning.bookkeeperlearning.entity.LearningModelEntity;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.List;

public class WalkForwardStd extends Validation {

    public WalkForwardStd(DataSource training, DataSource testing) {
        super(training,testing,"Bugginess classifier");
    }


    @Override
    public Evaluation buildModel(AbstractClassifier classifier ,Instances training,Instances testing,LearningModelEntity modelEntity) throws Exception {
        classifier.buildClassifier(training);
        Evaluation eval = new Evaluation(training);

        eval.evaluateModel(classifier, testing);
        return eval;
    }

}
