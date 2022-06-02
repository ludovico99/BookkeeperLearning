package org.learning.bookkeeperlearning.utility;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.statistics.BoxAndWhiskerCalculator;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.learning.bookkeeperlearning.entity.LearningModelEntity;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public  class BoxChart extends ApplicationFrame {

    private final JFreeChart chart;

    public BoxChart(String applicationTitle, String chartTitle, MetricsEnum metricsEnum, List<LearningModelEntity> learningModelEntityList) {
        super( applicationTitle );


        JFreeChart boxChart = ChartFactory.createBoxAndWhiskerChart(
                chartTitle,
                "MODELLO",
                metricsEnum.name(),
                createDataset(learningModelEntityList,metricsEnum),
                true);

        chart = boxChart;

        CategoryPlot plot =  boxChart.getCategoryPlot();
        plot.getDomainAxis().setMaximumCategoryLabelLines(2);

        plot.setRangeGridlinesVisible(true);



        ChartPanel chartPanel = new ChartPanel( boxChart );
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        chartPanel.setPreferredSize(new java.awt.Dimension(dim.width, dim.height));
        setContentPane( chartPanel );
    }

    public  DefaultBoxAndWhiskerCategoryDataset createDataset(List<LearningModelEntity> learningModelEntityList, MetricsEnum metricsEnum){

            final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

            int iterations = learningModelEntityList.get(0).getIterations();
            for (LearningModelEntity learningModelEntity : learningModelEntityList) {
                List<Double> values = new ArrayList<>();
                String classifier = learningModelEntity.getClassifier();
                for (int i = 0; i < iterations; i++) {
                    if (metricsEnum.equals(MetricsEnum.ACCURACY)) values.add(learningModelEntity.getAccuracy().get(i));
                    else if (metricsEnum.equals(MetricsEnum.RECALL)) values.add(learningModelEntity.getRecall().get(i));
                    else if (metricsEnum.equals(MetricsEnum.PRECISION))
                        values.add(learningModelEntity.getPrecision().get(i));
                    else if (metricsEnum.equals(MetricsEnum.KAPPA)) values.add(learningModelEntity.getKappa().get(i));
                    else values.add(learningModelEntity.getRocAuc().get(i));
                }

                BoxAndWhiskerItem item = BoxAndWhiskerCalculator.calculateBoxAndWhiskerStatistics(values);
                String columnKey = "Walk Forward " + learningModelEntity.getBalancing();
                if (learningModelEntity.isFeatureSelection()) columnKey = columnKey + " fs";
                dataset.add(item, classifier,columnKey);
        }
            return dataset;
        }

    public JFreeChart getChart() {
        return chart;
    }
}
