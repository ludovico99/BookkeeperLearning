package org.learning.bookkeeperLearning;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.List;

public class WalkForwardStd extends Validation {

    protected WalkForwardStd(DataSource training, DataSource testing) {
        super(training,testing,"Bugginess classifier");
    }

    @Override
    public List<LearningModelEntity> validation() throws Exception {
        createInstances();

        List<LearningModelEntity> results = initLearningModelEntities();

        for (int i = 0; i< trainings.size(); i++) {
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
                learningModelEntity.addFp(eval.numFalsePositives(1));
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
    public Evaluation buildModel(AbstractClassifier classifier ,Instances training,Instances testing,LearningModelEntity modelEntity) throws Exception {
        classifier.buildClassifier(training);
        Evaluation eval = new Evaluation(training);

        eval.evaluateModel(classifier, testing);
        return eval;
    }

}
