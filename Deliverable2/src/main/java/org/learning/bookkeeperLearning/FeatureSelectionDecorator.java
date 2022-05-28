package org.learning.bookkeeperLearning;

import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.util.List;

public class FeatureSelectionDecorator extends Decorator {

    protected FeatureSelectionDecorator(Validation validation) {
        super(validation.dataSetName + " with feature selection", validation);
    }

    @Override
    public List<LearningModelEntity> validation() throws Exception {

        createInstances();

        List<LearningModelEntity> results = initLearningModelEntities();


        for (int i = 0; i < trainings.size(); i++) {
            for (int j = 0; j < classifiers.size(); j++) {
                LearningModelEntity learningModelEntity = results.get(j);

                Evaluation eval = buildModel(classifiers.get(j),trainings.get(i),testings.get(i),learningModelEntity);

                double recall = Math.round(eval.recall(1) * 100.0) / 100.0;
                double precision = Math.round(eval.precision(1) * 100.0) / 100.0;
                double accuracy = Math.round(eval.pctCorrect() * 100.0) / 100.0;
                double auc = Math.round(eval.areaUnderROC(1) * 100.0) / 100.0;
                double kappa = Math.round(eval.kappa() * 100.0) / 100.0;


                learningModelEntity.addTp(eval.numTruePositives(1));
                learningModelEntity.addTn(eval.numTrueNegatives(1));
                learningModelEntity.addFp( eval.numFalsePositives(1));
                learningModelEntity.addFn(eval.numFalseNegatives(1));

                learningModelEntity.addAccuracy(accuracy);
                learningModelEntity.addRecall(recall);
                learningModelEntity.addPrecision(precision);
                learningModelEntity.addKappa(kappa);
                learningModelEntity.addRocAuc(auc);




            }
        }
        return results;
    }



    @Override
    public Evaluation buildModel(AbstractClassifier classifier, Instances training,Instances testing,LearningModelEntity modelEntity) throws Exception {

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

        Evaluation eval = this.getValidation().buildModel(classifier,trainingFiltered,testingFiltered,modelEntity);
        eval.evaluateModel(classifier, testingFiltered);

        return eval;
    }
}
