package main.java;

import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;

public class WekaLearning {


    public static void main(String[] args) throws Exception {
        //load datasets

        DataSource source1 = new DataSource("StormClassBugginessTraining.arff");
        DataSource source2 = new DataSource("StormClassBugginessTesting.arff");
        //BookkeeperClassBugginessTraining.arff
        //BookkeeperClassBugginessTesting.arff

        //StormClassBugginessTraining.arff
        //StormClassBugginessTesting.arff
        Validation walkForwardStd = new WalkForwardStd(source1,source2);

        List<LearningModelEntity> res = new ArrayList<>(walkForwardStd.validation());

        CsvOutput.addLines(res,source1.getDataSet().size());
        CsvOutput.getWriter().close();

//        Validation walkForwardWithBalancing = new BalancingDecorator(new WalkForwardStd(source1,source2),BalancingEnum.SMOTE_SAMPLING);
//
//        res.addAll(walkForwardWithBalancing.validation());
//
//        Validation walkForwardWithBalancingAndFeatureSelection = new FeatureSelectionDecorator(new BalancingDecorator(new WalkForwardStd(source1,source2),BalancingEnum.SMOTE_SAMPLING));
//
//        res.addAll(walkForwardWithBalancingAndFeatureSelection.validation());
//
//        BoxChart chart = walkForwardWithBalancingAndFeatureSelection.showChart(res,MetricsEnum.ACCURACY);
//
//        walkForwardWithBalancingAndFeatureSelection.saveChart(chart,"all");



    }

}


