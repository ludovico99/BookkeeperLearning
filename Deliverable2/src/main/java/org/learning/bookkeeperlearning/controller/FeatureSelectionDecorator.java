package org.learning.bookkeeperlearning.controller;

import org.learning.bookkeeperlearning.entity.LearningModelEntity;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.util.logging.Level;
import java.util.logging.Logger;


public class FeatureSelectionDecorator extends Decorator {

    private final Logger logger = Logger.getLogger("Feature selection log");

    public FeatureSelectionDecorator(Validation val) {
        super(val.validationEntity.getDataSetName() + " with feature selection", val);
    }


    @Override
    public Evaluation buildModel(AbstractClassifier classifier, Instances training,Instances testing,LearningModelEntity modelEntity) {
        try {
            modelEntity.setFeatureSelection(true);


            AttributeSelection filter = new AttributeSelection();
            //create evaluator and search algorithm objects
            CfsSubsetEval subsetEval = new CfsSubsetEval();
            GreedyStepwise search = new GreedyStepwise();

            //set the algorithm to search backward
            search.setSearchBackwards(true);
            //set the filter to use the evaluator and search algorithm
            filter.setEvaluator(subsetEval);

            filter.setSearch(search);
            //specify the dataset

            filter.setInputFormat(training);

            Instances trainingFiltered = Filter.useFilter(training, filter);
            Instances testingFiltered = Filter.useFilter(testing, filter);

            int numAttrFiltered = trainingFiltered.numAttributes();

            trainingFiltered.setClassIndex(numAttrFiltered - 1);
            testingFiltered.setClassIndex(numAttrFiltered - 1);

            Evaluation eval = this.getValidation().buildModel(classifier, trainingFiltered, testingFiltered, modelEntity);
            if(eval != null) eval.evaluateModel(classifier, testingFiltered);

            return eval;
        }catch (Exception e){
            logger.log(Level.SEVERE,"Error in feature selection evaluation",e);
        }
        return null;
    }
}
