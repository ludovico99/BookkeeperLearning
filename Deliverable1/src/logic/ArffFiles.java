package logic;


import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArffFiles {
    private static final String FILE_NAME_ARFF_TESTING = "C:\\Users\\ludov\\Desktop\\Anno 2021-2022\\ISW2\\Progetti\\Deliverable2\\BookkeeperClassBugginessTesting.arff";
    private static final String FILE_NAME_ARFF_TRAINING = "C:\\Users\\ludov\\Desktop\\Anno 2021-2022\\ISW2\\Progetti\\Deliverable2\\BookkeeperClassBugginessTraining.arff";
    private static FileWriter writerTraining;
    private static FileWriter writerTesting;

    //BookkeeperClassBugginessTesting.arff
    //BookkeeperClassBugginessTraining.arff
    //StormClassBugginessTesting.arff
    //StormClassBugginessTraining.arff


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

    public static void addLinesToDataSet(DataSet dataSet){
         FileWriter writer;
         int buggyClasses=0;
         if (dataSet.getClass().equals(TrainingSet.class))  writer = writerTraining;
         else writer = writerTesting;
         List<ReleaseEntity> releaseEntityList = dataSet.getReleases();
        try{
            String classToInsert;
            String versionConsidered;
            List<String> aux = new ArrayList<>();
            List<ReleaseEntity> versionsToBeConsidered =releaseEntityList.subList(0,releaseEntityList.size()/2+1);
            int rowsToBeConsidered =0;
            writer.append("@relation Class-Bugginess-DataSet\n");
            writer.append("@attribute version {");
            for (ReleaseEntity release: versionsToBeConsidered) {
                versionConsidered = release.getVersion();
                writer.append(versionConsidered);
                rowsToBeConsidered = rowsToBeConsidered + release.getJavaFiles().size();
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

            writer.append("""
                    @attribute size numeric
                    @attribute LOC_Touched numeric
                    @attribute NR numeric
                    @attribute LOC_added numeric
                    @attribute MAX_LOC_added numeric
                    @attribute AVG_LOC_added numeric
                    @attribute Churn numeric
                    @attribute MAX_churn numeric
                    @attribute AVG_churn_churn numeric
                    @attribute ChgSetSize numeric 
                    @attribute MAX_ChgSet numeric
                    @attribute AVG_ChgSet numeric
                    @attribute AGE numeric
                    @attribute weightedAge numeric
                    @attribute nAuthors numeric
                    @attribute bugginess {no,yes}""");

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
