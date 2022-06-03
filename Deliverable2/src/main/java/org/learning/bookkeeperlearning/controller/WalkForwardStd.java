package org.learning.bookkeeperlearning.controller;

import org.learning.bookkeeperlearning.entity.LearningModelEntity;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.logging.Level;
import java.util.logging.Logger;


public class WalkForwardStd extends Validation {

    private final Logger logger = Logger.getLogger("Standard walk forward log");

    public WalkForwardStd(DataSource training, DataSource testing) {
        super(training,testing,"Bugginess classifier");
    }


    @Override
    public Evaluation buildModel(AbstractClassifier classifier ,Instances training,Instances testing,LearningModelEntity modelEntity) {
        try {
            classifier.buildClassifier(training);
            Evaluation eval = new Evaluation(testing);

            eval.evaluateModel(classifier, testing);
            return eval;
        }catch (Exception e){
            logger.log(Level.SEVERE,"Error in walk forward evaluation",e);
        }
        return null;
    }

}
