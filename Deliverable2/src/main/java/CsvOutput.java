package main.java;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvOutput {

    private static final  String FILE_NAME=  "BookkeeperWalkForward.csv";
    private static FileWriter writer;
    //StormWalkForward.csv
    //BookkeeperWalkForward.csv

    static {
        try {
            writer = new FileWriter(FILE_NAME);
            writer.append("Classifier");
            writer.append(", ");
            writer.append("DataSet Name");
            writer.append(", ");
            writer.append("#Training release");
            writer.append(", ");

            writer.append("%Training");
            writer.append(", ");
            writer.append("#Defective Training");
            writer.append(", ");
            writer.append("#Defective Testing");
            writer.append(", ");
            writer.append("Balancing");
            writer.append(", ");
            writer.append("Feature Selection");
            writer.append(", ");
            writer.append("Sensitivity");
            writer.append(", ");

            writer.append("TP");
            writer.append(", ");
            writer.append("FP");
            writer.append(", ");
            writer.append("TN");
            writer.append(", ");
            writer.append("FN");
            writer.append(", ");


            writer.append("Recall");
            writer.append(", ");
            writer.append("Precision");
            writer.append(", ");
            writer.append("Accuracy");
            writer.append(", ");
            writer.append("ROC AUC");
            writer.append(", ");
            writer.append("Kappa");
            writer.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileWriter getWriter() {
        return writer;
    }


    private  CsvOutput() {
    }

    public static void addLines(List<LearningModelEntity> outputs,int total){
        try{

            for (int i =0 ; i<outputs.get(0).getIterations();i++) {
                for (LearningModelEntity line : outputs) {
                    int numAttr = line.getTrainings().get(i).numAttributes();
                    writer.append(line.getClassifier());
                    writer.append(", ");
                    writer.append(line.getDataSetName());
                    writer.append(", ");
                    writer.append(String.valueOf(i+1));
                    writer.append(", ");

                    writer.append(String.valueOf(Math.round((line.getTrainings().get(i).size()*1000 / (double)total))/(double)10));
                    writer.append(", ");

                    writer.append(String.valueOf(line.getTrainings().get(i).attributeStats(numAttr - 1).nominalCounts[1]));
                    writer.append(", ");

                    writer.append(String.valueOf(line.getTestings().get(i).attributeStats(numAttr - 1).nominalCounts[1]));
                    writer.append(", ");

                    writer.append(line.getBalancing());
                    writer.append(", ");

                    writer.append(String.valueOf(line.isFeatureSelection()));
                    writer.append(", ");

                    writer.append(String.valueOf(false));
                    writer.append(", ");

                    writer.append(String.valueOf(line.getTp().get(i)));
                    writer.append(", ");

                    writer.append(String.valueOf(line.getFp().get(i)));
                    writer.append(", ");

                    writer.append(String.valueOf(line.getTn().get(i)));
                    writer.append(", ");

                    writer.append(String.valueOf(line.getFn().get(i)));
                    writer.append(", ");


                    writer.append(String.valueOf(line.getRecall().get(i)));
                    writer.append(", ");
                    writer.append(String.valueOf(line.getPrecision().get(i)));
                    writer.append(", ");
                    writer.append(String.valueOf(line.getAccuracy().get(i)));
                    writer.append(", ");
                    writer.append(String.valueOf(line.getRocAuc().get(i)));
                    writer.append(", ");
                    writer.append(String.valueOf(line.getKappa().get(i)));
                    writer.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
