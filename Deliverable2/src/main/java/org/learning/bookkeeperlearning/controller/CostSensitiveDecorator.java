package org.learning.bookkeeperlearning.controller;

import org.learning.bookkeeperlearning.entity.LearningModelEntity;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CostSensitiveDecorator extends Decorator{

    private final Logger logger = Logger.getLogger("Cost sensitive log");


    public CostSensitiveDecorator(Validation val) {
        super(val.validationEntity.getDataSetName() + " with cost sensitive", val);
    }

    private CostMatrix createCostMatrix() {
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(1, 0, 10.0);
        costMatrix.setCell(0, 1, 1.0);
        costMatrix.setCell(1, 1, 0.0);
        return costMatrix;
    }


    @Override
    public Evaluation buildModel(AbstractClassifier classifier, Instances training, Instances testing, LearningModelEntity modelEntity) {
        try {
            CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
            costSensitiveClassifier.setClassifier(classifier);

            costSensitiveClassifier.setCostMatrix(createCostMatrix());

            Evaluation eval = this.getValidation().buildModel(costSensitiveClassifier, training, testing, modelEntity);

            if(eval != null) eval.evaluateModel(costSensitiveClassifier, testing);

            modelEntity.setCostSensitive(true);

            return eval;

        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in cost sensitive evaluation",e);
        }
        return null;
    }
}
