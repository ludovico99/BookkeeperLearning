package org.learning.bookkeeperlearning.controller;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.learning.bookkeeperlearning.entity.CommitEntity;
import org.learning.bookkeeperlearning.entity.JavaFileEntity;
import org.learning.bookkeeperlearning.entity.JiraTicketsEntity;
import org.learning.bookkeeperlearning.entity.ReleaseEntity;
import org.learning.bookkeeperlearning.utilityclasses.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeaturesController {

    private static final String DATEFORMAT = "yyyy-MM-dd";


    private void  releaseInitialization (ReleaseEntity commitVersion, ReleaseEntity prevVersion, Date dateRelease) {
        /* <-- copia delle classi nella versione successiva, a partire dallo stato della versione precedente
         * All'inizio della release successiva ci sono tutte le classi della release precedente.
         *
         * */

        JavaFileEntity newClass;
        List<JavaFileEntity> prevVersionJavaFiles = prevVersion.getJavaFiles();
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        try {
            for (JavaFileEntity k : prevVersionJavaFiles) {
                long prevReleaseTime = sdf.parse(prevVersion.getDate()).getTime();
                double newAge = k.getAge() + ((dateRelease.getTime() -
                        prevReleaseTime) / (double) 604800000);
                //<-- Assumo che sia presente anche per tutta la release successiva, se non è cosi viene eliminta
                //da una DELETE nella release
                newClass = new JavaFileEntity(k.getClassName(), commitVersion.getVersion(), k.getSize(),
                        newAge, k.getAuthors());
                commitVersion.addJavaFile(newClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void analyzeChanges(DiffEntry diff, RevCommit rev,
                                double age, EditList editList,ReleaseEntity commitVersion,
                                JavaFileEntity javaFile,String fileName, int dfsSize) {

        int[] newChanges = new int[] {0,0,0};

        for (Edit edit : editList) {
            if (edit.getType().compareTo(Edit.Type.INSERT) == 0) {
                newChanges[0] += edit.getLengthB();
            }

            if (edit.getType().compareTo(Edit.Type.DELETE) == 0) {
                newChanges[1] += edit.getLengthA();
            }

            if (edit.getType().compareTo(Edit.Type.REPLACE) == 0) {
                newChanges[2] += edit.getLengthB() - edit.getLengthA();
            }
        }

        switch (diff.getChangeType()) {
            case ADD:
                addHandler(javaFile,rev,age,fileName,commitVersion,dfsSize,newChanges);
                break;
            case DELETE:
                commitVersion.getJavaFiles().remove(javaFile);
                break;

            default:
                modifyHandler(rev,javaFile,newChanges, dfsSize);
                break;
        }
    }

    private void addHandler(JavaFileEntity javaFile,RevCommit rev,double age,
                            String fileName, ReleaseEntity commitVersion, int dfsSize, int[] newChanges) {

        int newInsert = newChanges[0];
        int newReplace = newChanges[2];
        int newDelete = newChanges[1];

        int locAdded = newInsert;
        if (newReplace > 0) locAdded = locAdded + newReplace;
        if (javaFile == null) {
            int value = newInsert - newDelete + newReplace;
            int locTouched = newInsert + Math.abs(newReplace) + newDelete;
            List<String> authors = new ArrayList<>();
            authors.add(rev.getCommitterIdent().getName());
            double weightedAge = age * (newInsert + Math.abs(newReplace) + newDelete);
            int[] aux = new int[]{value, locTouched, locAdded, dfsSize};

            JavaFileEntity newClass = new JavaFileEntity(fileName, aux, commitVersion.getVersion(),
                    age, weightedAge, authors);
            commitVersion.addJavaFile(newClass);
        }
    }

    private void modifyHandler (RevCommit rev, JavaFileEntity javaFile, int[] newChanges, int dfsSize) {

        int newInsert = newChanges[0];
        int newDelete = newChanges[1];
        int newReplace = newChanges[2];

        if (javaFile != null) {
            /*Una revisione di una classe è il numero di volte in cui la classe è stata toccata
             da un commit diverso in quella versione  */
            int newNR = javaFile.getNr() + 1;
            int size = javaFile.getSize() + newInsert - newDelete + newReplace;
            int locTouched = javaFile.getLocTouched()
                    + newDelete + newInsert + Math.abs(newReplace);
            int locAdded = javaFile.getLocAdded() + newInsert;
            if (newReplace > 0) locAdded = locAdded + newReplace;
            int maxLocAdded = Math.max(javaFile.getMaxLocAdded(), locAdded);
            double avgLocAdded = locAdded / (double) newNR;
            int churn = javaFile.getChurn() + newInsert - newDelete + newReplace;
            int maxChurn = Math.max(javaFile.getMaxChurn(), newInsert - newDelete + newReplace);
            double avgChurn = churn / (double) newNR;
            int chgSetSize = javaFile.getChgSetSize() + dfsSize - 1;
            int maxChgSet = Math.max(javaFile.getMaxChgSetSize(), dfsSize - 1);
            double avgChgSet = chgSetSize / (double) newNR;
            List<String> authors = javaFile.getAuthors();
            if (!authors.contains(rev.getCommitterIdent().getName()))
                authors.add(rev.getCommitterIdent().getName());

            javaFile.setNr(newNR);
            javaFile.setSize(size);
            javaFile.setLocTouched(locTouched);
            javaFile.setLocAdded(locAdded);
            javaFile.setMaxLocAdded(maxLocAdded);
            javaFile.setAvgLocAdded(avgLocAdded);
            javaFile.setChurn(churn);
            javaFile.setMaxChurn(maxChurn);
            javaFile.setAvgChurn(avgChurn);
            javaFile.setChgSetSize(chgSetSize);
            javaFile.setMaxChgSetSize(maxChgSet);
            javaFile.setAvgChgSetSize(avgChgSet);
            javaFile.setWeightedAge(javaFile.getAge() * locTouched);
        }
    }

    public void getFeaturesPerVersionsAndClasses(List<CommitEntity> log, List<ReleaseEntity> releaseEntityList) {
        ReleaseEntity prevVersion = log.get(0).getVersion();
        try {

            Repository repo = GitController.getGit().getRepository();
            SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);


            if (repo != null) {
                for (CommitEntity commit : log) {
                    RevCommit rev =commit.getCommit();
                    Date date = rev.getCommitterIdent().getWhen();
                    ReleaseEntity commitVersion = commit.getVersion();
                    List<DiffEntry> dfs = GitController.diffCommit(rev.getName());
                    if (dfs == null || commitVersion == null) continue;
                    Date dateRelease = new Date(sdf.parse(commitVersion.getDate()).getTime());

                    if (!commitVersion.getVersion().equals(prevVersion.getVersion())) {
                        releaseInitialization (commitVersion,prevVersion, dateRelease);
                    }


                    double age = (dateRelease.getTime() - date.getTime()) / (double)604800000;

                    for (DiffEntry diff : dfs) {
                        String javaSuffix = ".java";
                        if (diff.getOldPath().endsWith(javaSuffix) || diff.getNewPath().endsWith(javaSuffix)) {
                            String[] newName = diff.getNewPath().split("/");
                            if (newName[newName.length-1].contains("test") || newName[newName.length-1].contains("Test") ) continue;
                            String fileName;

                            if (DiffEntry.ChangeType.DELETE.equals(diff.getChangeType()))
                                fileName = diff.getOldPath();
                            else fileName = diff.getNewPath();

                            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                            df.setRepository(repo);

                            FileHeader fileHeader = df.toFileHeader(diff);
                            EditList editList = fileHeader.toEditList();

                            JavaFileEntity javaFile = isJavaFilePresentInThatVersion(commitVersion,fileName);

                            analyzeChanges(diff,rev,age,editList,commitVersion,javaFile,fileName,dfs.size());

                        }
                    }
                    prevVersion = commitVersion;
                }
                removeReleasesNotInterestedByACommit(releaseEntityList);
            }


        } catch (Exception e) {
            e.printStackTrace();
            GitController.getGit().close();
        }
    }

    private JavaFileEntity isJavaFilePresentInThatVersion(ReleaseEntity commitVersion, String fileName){
        // Ritorna null se quella classe non è ancora presente tra le classi di quella release
        JavaFileEntity javaFile = null;
        for (JavaFileEntity file : commitVersion.getJavaFiles()) {
            if (file.getClassName().equals(fileName)) {
                javaFile = file;
            }
        }
        return javaFile;
    }

     private void removeReleasesNotInterestedByACommit (List<ReleaseEntity> releaseEntityList){
         for (int i=releaseEntityList.size() - 1; i>=0;i--){
             if (releaseEntityList.get(i).getJavaFiles().isEmpty()) {
                 //<-- non ho commit per quella versione
                 releaseEntityList.remove(i); //rimuovo le versioni non interessate da nessun commit
             }
         }
     }

    public List<CommitEntity> bindCommitsAndTickets(List<RevCommit> commits, List<ReleaseEntity> releaseEntityList, List<JiraTicketsEntity> jiraTicketsEntityList) throws ParseException {
        //Creo una lista di classi Commit con la versione e se presente ticket Id corrispondente
        List<CommitEntity> commitEntityList = new ArrayList<>();
        boolean isPresent;
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        String[] strTokenized;
        for (RevCommit rev : commits) {
            Date date = rev.getCommitterIdent().getWhen();
            ReleaseEntity commitVersion =
                    Utilities.getVersionByDate(releaseEntityList, sdf.format(date));
            isPresent = false;
            for (JiraTicketsEntity ticket : jiraTicketsEntityList) {
                if (rev.getFullMessage().contains(ticket.getKey())) {//<--commit con quel ticket ID
                    strTokenized = ticket.getKey().split("-");
                    int len = strTokenized[strTokenized.length - 1].length();
                    String[] str = rev.getFullMessage().split( "BOOKKEEPER-");
                    String[] numberTicketID = str[str.length - 1].split(":");
                    if (numberTicketID[0].matches(String.format("[0-9]{%d}", len))) {
                        CommitEntity commit = new CommitEntity(rev, commitVersion, ticket);
                        commitEntityList.add(commit);
                        isPresent = true;
                        break;
                    }
                }
            }
            if(!isPresent) {
                CommitEntity entity = new CommitEntity(rev,commitVersion);
                commitEntityList.add(entity);
            }
        }
        return commitEntityList;
    }
}
