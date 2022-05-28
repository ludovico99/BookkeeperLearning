package org.learning.bookkeeperLearning;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public  class BoxChart extends ApplicationFrame {

    private final JFreeChart chart;

    protected BoxChart(String applicationTitle , String chartTitle,MetricsEnum metricsEnum, List<LearningModelEntity> learningModelEntityList) {
        super( applicationTitle );
        JFreeChart barChart = ChartFactory.createBoxAndWhiskerChart(
                chartTitle,
                "MODELLO",
                metricsEnum.name(),
                createDataset(learningModelEntityList,metricsEnum),
                true);

        chart = barChart;

        ChartPanel chartPanel = new ChartPanel( barChart );
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        chartPanel.setPreferredSize(new java.awt.Dimension(dim.width, dim.height));
        setContentPane( chartPanel );
    }

    public  DefaultBoxAndWhiskerCategoryDataset createDataset(List<LearningModelEntity> learningModelEntityList, MetricsEnum metricsEnum){

            final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

            int iterations = learningModelEntityList.get(0).getIterations();
            for (int j=0; j<learningModelEntityList.size(); j++) {
                List<Double> values = new ArrayList<>();
                String classifier = learningModelEntityList.get(j).getClassifier();
                for (int i = 0; i < iterations; i++) {
                    if (metricsEnum.equals(MetricsEnum.ACCURACY)) values.add(learningModelEntityList.get(j).getAccuracy().get(i));
                    else if (metricsEnum.equals(MetricsEnum.RECALL))   values.add(learningModelEntityList.get(j).getRecall().get(i));
                    else if (metricsEnum.equals(MetricsEnum.PRECISION))  values.add(learningModelEntityList.get(j).getPrecision().get(i));
                    else if (metricsEnum.equals(MetricsEnum.KAPPA))  values.add(learningModelEntityList.get(j).getKappa().get(i));
                    else values.add(learningModelEntityList.get(j).getRocAuc().get(i));
                }

                BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(values);
                dataset.add(item, classifier , "modello " + (j+1) );
            }
            return dataset;
        }

    public JFreeChart getChart() {
        return chart;
    }
}
