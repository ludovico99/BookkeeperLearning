package logic;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

import java.util.List;

public class BalancingDecorator extends Decorator{

    private final BalancingEnum value;

    protected BalancingDecorator(Validation val, BalancingEnum value) {
        super(val.dataSetName + " with balancing techniques",val);
        this.value = value;

    }

    @Override
    public List<LearningModelEntity> validation() throws Exception {

        createInstances();

        List<LearningModelEntity> results = initLearningModelEntities();

        for (int i = 0; i < trainings.size(); i++) {
            for (int j = 0; j < classifiers.size(); j++) {
                LearningModelEntity learningModelEntity = results.get(j);

                Evaluation eval = buildModel(classifiers.get(j),trainings.get(i),testings.get(i),learningModelEntity);
                //sampled

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

    public void initSmote(FilteredClassifier fc, Instances training) throws Exception {
        SMOTE smote = new SMOTE();
        smote.setInputFormat(training);
        fc.setFilter(smote);
    }

    public void initUnderSampling(FilteredClassifier fc) throws Exception {
        SpreadSubsample spreadSubsample = new SpreadSubsample();
        String[] opts = new String[]{"-M", "1.0"};
        spreadSubsample.setOptions(opts);
        fc.setFilter(spreadSubsample);
    }

    public void initOverSampling(FilteredClassifier fc,Instances training) throws Exception {
        int majority;
        int minority;
        Resample resample = new Resample();
        int numAttr = training.numAttributes();


        majority = training.attributeStats(numAttr - 1).nominalCounts[0];
        minority = training.attributeStats(numAttr - 1).nominalCounts[1];

        double sampleSizePercent;

        if(majority < minority)  sampleSizePercent  = 100 * (minority - majority) / (double) majority;
        else sampleSizePercent  = 100 * (majority - minority) / (double) minority;

        String[] opts = new String[]{"-B", "1.0", "-Z", String.valueOf(sampleSizePercent)};
        resample.setOptions(opts);
        fc.setFilter(resample);
    }


    @Override
    public Evaluation buildModel(AbstractClassifier classifier, Instances training, Instances testing,LearningModelEntity modelEntity) throws Exception {
        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(classifier);

        if(value.equals(BalancingEnum.SMOTE_SAMPLING)){
            initSmote(fc,training);
            modelEntity.setBalancing("SMOTE-sampling");
        }
        else if (value.equals(BalancingEnum.OVER_SAMPLING)){
            initOverSampling(fc,training);
            modelEntity.setBalancing("OVER-sampling");
        }
        else {
            initUnderSampling(fc);
            modelEntity.setBalancing("UNDER-sampling");
        }

        fc.buildClassifier(training);

        Evaluation eval = this.getValidation().buildModel(fc,training,testing,modelEntity);

        eval.evaluateModel(fc, testing);

        return eval;
    }
}
