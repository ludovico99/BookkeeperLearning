package org.learning.bookkeeperlearning.controller;

import org.learning.bookkeeperlearning.entity.LearningModelEntity;
import org.learning.bookkeeperlearning.utility.FeatureSelectionEnum;
import weka.attributeSelection.*;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.util.logging.Level;
import java.util.logging.Logger;


public class FeatureSelectionDecorator extends Decorator {

    private final Logger logger = Logger.getLogger("Feature selection log");
    private final FeatureSelectionEnum value;

    public FeatureSelectionDecorator(Validation val,FeatureSelectionEnum value) {
        super(val.validationEntity.getDataSetName() + " with feature selection", val);
        this.value = value;
    }

    private void initBackwardsSearch(AttributeSelection filter){
        CfsSubsetEval subsetEval = new CfsSubsetEval();
        GreedyStepwise search = new GreedyStepwise();

        //set the algorithm to search backward
        search.setSearchBackwards(true);

        //set the filter to use the evaluator and search algorithm
        filter.setEvaluator(subsetEval);

        filter.setSearch(search);
        //specify the dataset

    }

    private void initWrapperForwardsSearch(AbstractClassifier classifier, AttributeSelection filter){
        ClassifierSubsetEval subsetEval = new ClassifierSubsetEval();

        subsetEval.setClassifier(classifier); //<-- WRAPPER

        GreedyStepwise search = new GreedyStepwise();

        filter.setEvaluator(subsetEval);

        filter.setSearch(search);

    }

    private void initWrapperBackwardsSearch(AbstractClassifier classifier, AttributeSelection filter){
        ClassifierSubsetEval subsetEval = new ClassifierSubsetEval();

        subsetEval.setClassifier(classifier); //<-- WRAPPER

        GreedyStepwise search = new GreedyStepwise();
        search.setSearchBackwards(true);

        filter.setEvaluator(subsetEval);

        filter.setSearch(search);

    }

    private void initForwardsSearch(AttributeSelection filter){
        CfsSubsetEval subsetEval = new CfsSubsetEval();

        GreedyStepwise search = new GreedyStepwise();

        filter.setEvaluator(subsetEval);

        filter.setSearch(search);

    }


    private void initBestFirst (AttributeSelection filter){
        CfsSubsetEval subsetEval = new CfsSubsetEval();

        BestFirst search= new BestFirst();

        filter.setEvaluator(subsetEval);

        filter.setSearch(search);

    }

    private void initCorrEvaluator (AttributeSelection filter){
        CorrelationAttributeEval correlationAttributeEval = new CorrelationAttributeEval();

        Ranker search= new Ranker();

        //set the filter to use the evaluator and search algorithm
        filter.setEvaluator(correlationAttributeEval);

        filter.setSearch(search);
    }


    @Override
    public Evaluation buildModel(AbstractClassifier classifier, Instances training,Instances testing,LearningModelEntity modelEntity) {
        try {
            modelEntity.setFeatureSelection(true);

            AttributeSelection filter = new AttributeSelection();
            //create evaluator and search algorithm objects


            if (value.equals(FeatureSelectionEnum.FILTER_BACKWARDS_SEARCH)) {
                initBackwardsSearch(filter);
                modelEntity.setTypeFeatureSelection("Backwards search");
            } else if (value.equals(FeatureSelectionEnum.WRAPPER_FORWARDS_SEARCH)) {
                initWrapperForwardsSearch(classifier, filter);
                modelEntity.setTypeFeatureSelection("WRAPPER Forwards search");
            }else if (value.equals(FeatureSelectionEnum.FILTER_FORWARDS_SEARCH)){
                initForwardsSearch(filter);
                modelEntity.setTypeFeatureSelection("Forwards search");
            }else if (value.equals(FeatureSelectionEnum.BEST_FIRST)) {
                initBestFirst(filter);
                modelEntity.setTypeFeatureSelection("Best first");
            } else if(value.equals(FeatureSelectionEnum.WRAPPER_BACKWARDS_SEARCH)){
              initWrapperBackwardsSearch(classifier,filter);
              modelEntity.setTypeFeatureSelection("WRAPPER Backwards search");
            } else{
                initCorrEvaluator(filter);
                modelEntity.setTypeFeatureSelection("Ranker");
            }


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
