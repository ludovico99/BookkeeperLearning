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
import org.learning.bookkeeperlearning.utility.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeaturesController {

    private static final String DATEFORMAT = "yyyy-MM-dd";

    private final Logger logger = Logger.getLogger("Features controller log");


    private void  releaseInitialization (ReleaseEntity commitVersion, ReleaseEntity prevVersion, Date dateRelease) {
        /* <-- copia delle classi nella versione successiva, a partire dallo stato della versione precedente
         * All'inizio della release successiva ci sono tutte le classi della release precedente.
         *
         * */
        if (!commitVersion.getVersion().equals(prevVersion.getVersion())) {
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
                logger.log(Level.SEVERE,"Error in parsing dates" ,e);
            }
        }
    }

    private int[] analyzeEditList(EditList editList) {

        int[] newChanges = new int[] {0,0,0};

        for (Edit edit : editList) {
            switch (edit.getType()) {
                case INSERT:
                    newChanges[0] += edit.getLengthB();
                    break;
                case DELETE:
                    newChanges[1] += edit.getLengthA();
                    break;

                case REPLACE:
                    if (edit.getLengthA() < edit.getLengthB()) {
                        newChanges[0] += edit.getLengthB() - edit.getLengthA();
                    } else if (edit.getLengthA() > edit.getLengthB()) {
                        newChanges[1] += edit.getLengthA() - edit.getLengthB();
                    }
                    break;
                default:
                    break;
            }
        }

        return newChanges;
    }

    private void analyzeChanges (DiffEntry diff, String newAuthor,
                                 double age, ReleaseEntity commitVersion,
                                     int[] newChanges){

        JavaFileEntity javaFile =  isJavaFilePresentInThatVersion(commitVersion, diff.getNewPath());

        switch (diff.getChangeType()) {

            case ADD:
                addHandler(javaFile,newAuthor,age,diff.getNewPath(),commitVersion,newChanges);
                break;

            case DELETE:
                javaFile = isJavaFilePresentInThatVersion(commitVersion, diff.getOldPath());
                commitVersion.getJavaFiles().remove(javaFile);
                break;

            default:
                modifyHandler(newAuthor,javaFile,newChanges);
                break;
        }

    }

    private void addHandler(JavaFileEntity javaFile,String newAuthor,double age,
                            String fileName, ReleaseEntity commitVersion, int[] newChanges) {

        int newInsert = newChanges[0];
        int newDelete = newChanges[1];
        int dfsSize =  newChanges[2];

        if (javaFile == null) {
            List<String> authors = new ArrayList<>();
            authors.add(newAuthor);
            int[] aux = new int[]{newInsert - newDelete, newInsert + newDelete, newInsert, dfsSize};
            JavaFileEntity newClass = new JavaFileEntity(fileName, aux, commitVersion.getVersion(),
                    age, age * (newInsert  + newDelete), authors);
            commitVersion.addJavaFile(newClass);
        }
    }

    private void modifyHandler (String author, JavaFileEntity javaFile, int[] newChanges) {

        int newInsert = newChanges[0];
        int newDelete = newChanges[1];
        int dfsSize = newChanges [2];

        if (javaFile != null) {
            /*Una revisione di una classe è il numero di volte in cui la classe è stata toccata
             da un commit diverso in quella versione  */

            javaFile.addNr(1);
            javaFile.addSize(newInsert - newDelete);
            javaFile.addLocTouched(newDelete + newInsert);
            javaFile.addLocAdded(newInsert);
            javaFile.addChurn(newInsert - newDelete);
            javaFile.addChgSetSize(dfsSize - 1);
            javaFile.setWeightedAge(javaFile.getAge() * javaFile.getLocTouched());
            javaFile.addAuthors(author);
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

                    releaseInitialization (commitVersion,prevVersion, dateRelease);

                    double age = (dateRelease.getTime() - date.getTime()) / (double)604800000;

                    for (DiffEntry diff : dfs) {

                        if (Boolean.FALSE.equals(Utilities.isAJavaFileExcludeTests(diff))) continue;

                        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                        df.setRepository(repo);

                        FileHeader fileHeader = df.toFileHeader(diff);

                        int[] changes = analyzeEditList(fileHeader.toEditList());
                        changes[2] = dfs.size();

                        analyzeChanges(diff, rev.getCommitterIdent().getName(),
                                age, commitVersion, changes);
                    }

                    prevVersion = commitVersion;
                }
                removeReleasesNotInterestedByACommit(releaseEntityList);
            }


        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in retrieving file header" ,e);
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
        for (RevCommit rev : commits) {
            Date date = rev.getCommitterIdent().getWhen();
            ReleaseEntity commitVersion =
                    Utilities.getVersionByDate(releaseEntityList, sdf.format(date));
            isPresent = false;
            for (JiraTicketsEntity ticket : jiraTicketsEntityList) {
                if (Utilities.isTicketExactlyContained(rev.getFullMessage(), ticket.getKey())) {//<--commit con quel ticket ID
                    CommitEntity commit = new CommitEntity(rev, commitVersion, ticket);
                    commitEntityList.add(commit);
                    isPresent = true;
                    break;
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
