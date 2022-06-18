package org.learning.bookkeeperlearning.utility;
;
import org.learning.bookkeeperlearning.entity.DataSetEntity;
import org.learning.bookkeeperlearning.entity.JavaFileEntity;
import org.learning.bookkeeperlearning.entity.ReleaseEntity;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArffFiles {
    private static final String FILE_NAME_ARFF_TESTING = ".\\Deliverable2\\src\\main\\resources\\BookkeeperClassBugginessTesting.arff";
    private static final String FILE_NAME_ARFF_TRAINING = ".\\Deliverable2\\src\\main\\resources\\BookkeeperClassBugginessTraining.arff";

    private static FileWriter writerTraining;
    private static FileWriter writerTesting;


     private ArffFiles() {
     }

    static {
        try {
            writerTesting = new FileWriter(FILE_NAME_ARFF_TESTING);
            writerTraining = new FileWriter(FILE_NAME_ARFF_TRAINING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public  static FileWriter getWriterTraining() {
        return writerTraining;
    }

    public static FileWriter getWriterTesting() {
        return writerTesting;
    }

    public static void addLinesToDataSet(DataSetEntity dataSet, boolean training){
         FileWriter writer;
         int buggyClasses=0;
         if (training)  writer = writerTraining;
         else writer = writerTesting;
         List<ReleaseEntity> releaseEntityList = dataSet.getReleases();
        try{
            String classToInsert;
            String versionConsidered;
            List<String> aux = new ArrayList<>();
            List<ReleaseEntity> versionsToBeConsidered =releaseEntityList.subList(0,releaseEntityList.size()/2+1);
            writer.append("@relation Class-Bugginess-DataSet\n");
            writer.append("@attribute version {");
            for (ReleaseEntity release: versionsToBeConsidered) {
                versionConsidered = release.getVersion();
                writer.append(versionConsidered);
                writer.append(',');
            }
            writer.append("}\n");
            writer.append("@attribute FileName {");
            for (ReleaseEntity release : versionsToBeConsidered) {
                for (JavaFileEntity javaFile : release.getJavaFiles()) {
                    classToInsert = javaFile.getClassName();
                    if(javaFile.getBugginess().equals("yes")) buggyClasses++;
                    if (aux.contains(classToInsert)) continue;
                    writer.append(classToInsert);
                    writer.append(',');
                    aux.add(classToInsert);
                }
            }
            writer.append("}\n");

            writer.append("@attribute size numeric\n" +
                    "@attribute LOC_Touched numeric\n" +
                    "@attribute NR numeric\n"+
                    "@attribute LOC_added numeric\n"+
                    "@attribute MAX_LOC_added numeric\n"+
                    "@attribute AVG_LOC_added numeric\n"+
                    "@attribute Churn numeric\n"+
                    "@attribute MAX_churn numeric\n"+
                    "@attribute AVG_churn_churn numeric\n"+
                    "@attribute ChgSetSize numeric\n"+
                    "@attribute MAX_ChgSet numeric\n"+
                    "@attribute AVG_ChgSet numeric\n"+
                    "@attribute AGE numeric\n"+
                    "@attribute weightedAge numeric\n"+
                    "@attribute nAuthors numeric\n" +
                    "@attribute bugginess {no,yes}");

            writer.append("\n");
            writer.append("@data\n");

            int count=0;
            for (ReleaseEntity release : versionsToBeConsidered) {
                for (JavaFileEntity javaFile : release.getJavaFiles()) {
                    count++;
                    writer.append(javaFile.getVersion());
                    writer.append(",");
                    classToInsert = javaFile.getClassName();
                    writer.append(classToInsert);
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getSize()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getLocTouched()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getNr()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getLocAdded()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getMaxLocAdded()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getAvgLocAdded()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getChurn()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getMaxChurn()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getAvgChurn()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getChgSetSize()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getMaxChgSetSize()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getAvgChgSetSize()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getAge()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getWeightedAge()));
                    writer.append(',');
                    writer.append(String.valueOf(javaFile.getAuthors().size()));
                    writer.append(',');
                    writer.append(javaFile.getBugginess());
                    writer.append('\n');
                }
            }
            Logger.getAnonymousLogger().log(Level.INFO,"Numero di computed features inserite in half releases: {0}", count);
            Logger.getAnonymousLogger().log(Level.INFO,"Numero di buggy classes inserite in half releases: {0}", buggyClasses);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
