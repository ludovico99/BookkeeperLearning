package org.learning.bookkeeperlearning;

import org.learning.bookkeeperlearning.controller.*;
import org.learning.bookkeeperlearning.entity.LearningModelEntity;
import org.learning.bookkeeperlearning.utility.BalancingEnum;
import org.learning.bookkeeperlearning.utility.CsvOutput;
import org.learning.bookkeeperlearning.utility.MetricsEnum;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.List;

public class WekaLearning {


    public static void main(String[] args) throws Exception {

        /* FASE 1: Da deliverable 1 vengono generati due arff files. Il training set utilizza
         * p calcolato come media dalla release da 0 fino a fv del ticket - 1 (p non risente di dati futuri=.
         * Il testing set utilizza p pari alla media su tutte le release
         *
         * 1.1: Applico walk forward standard, senza "decorazioni" aggiuntive.
         * 1.2: Applico walk forward con feature selection.
         * 1.3: Applico walk forward con SMOTE SAMPLING.
         * 1.4: Applico walk forward con SMOTE SAMPLING e feature selection.
         * 1.5: Applico walk forward con UNDER SAMPLING e feature selection.
         * 1.6: Applico walk forward con OVER SAMPLING e feature selection.
         * 1.7: Applico walk forward con OVER SAMPLING e miss classifications cost.
         * 1.8: Realizzo un box chart categorico basato sulla metrica di accuratezza.
         *
         *
         * */


        DataSource source1 = new DataSource(".\\Deliverable2\\src\\main\\resources\\BookkeeperClassBugginessTraining.arff");
        DataSource source2 = new DataSource(".\\Deliverable2\\src\\main\\resources\\BookkeeperClassBugginessTesting.arff");

        Validation val = new WalkForwardStd(source1,source2);

        List<LearningModelEntity> res = new ArrayList<>(val.validation());

        val = new FeatureSelectionDecorator(new WalkForwardStd(source1,source2));

        res.addAll(val.validation());

        val = new BalancingDecorator(new WalkForwardStd(source1,source2), BalancingEnum.SMOTE_SAMPLING);

        res.addAll(val.validation());

        val = new FeatureSelectionDecorator(new BalancingDecorator(new WalkForwardStd(source1,source2),BalancingEnum.SMOTE_SAMPLING));

        res.addAll(val.validation());

        val = new FeatureSelectionDecorator(new BalancingDecorator(new WalkForwardStd(source1,source2),BalancingEnum.UNDER_SAMPLING));

        res.addAll(val.validation());

        val = new FeatureSelectionDecorator(new BalancingDecorator(new WalkForwardStd(source1,source2),BalancingEnum.OVER_SAMPLING));

        res.addAll(val.validation());

        val = new FeatureSelectionDecorator(new CostSensitiveDecorator(new WalkForwardStd(source1,source2)));

        res.addAll(val.validation());

        CsvOutput.addLines(res,source1.getDataSet().size());
        CsvOutput.getWriter().close();

        val.saveChart(val.showChart(res, MetricsEnum.ACCURACY),"ACCURACY_ALL");

        val.saveChart(val.showChart(res, MetricsEnum.ROCAUC),"ROC_All");

        val.saveChart(val.showChart(res, MetricsEnum.KAPPA),"KAPPA_All");

        val.saveChart(val.showChart(res, MetricsEnum.PRECISION),"PRECISION_All");

        val.saveChart(val.showChart(res, MetricsEnum.RECALL),"RECALL_All");




    }

}


