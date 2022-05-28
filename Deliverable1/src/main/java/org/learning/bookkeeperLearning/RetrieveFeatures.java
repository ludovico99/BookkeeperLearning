package org.learning.bookkeeperLearning;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RetrieveFeatures {

    private static final String REG_EX = "BOOKKEEPER-";
    //BOOKKEEPER
    //STORM

    private static final String JAVA_FILES = ".java";

    public void getFeaturesPerVersionsAndClasses(List<CommitEntity> log, List<ReleaseEntity> releaseEntityList) {
        ReleaseEntity prevVersion = log.get(0).getVersion();
        try {

            Repository repo = RetrieveGitInfo.getGit().getRepository();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            JavaFileEntity newClass;

            if (repo != null) {
                for (CommitEntity commit : log) {
                    RevCommit rev =commit.getCommit();
                    Date date = rev.getCommitterIdent().getWhen();
                    ReleaseEntity commitVersion = commit.getVersion();
                    List<DiffEntry> dfs = RetrieveGitInfo.diffCommit(rev.getName());
                    if (dfs == null || commitVersion == null) continue;
                    Date dateRelease = new Date(sdf.parse(commitVersion.getDate()).getTime());

                    if (!commitVersion.getVersion().equals(prevVersion.getVersion())) { //<-- copia delle classi nella versione
                        //successiva, a partire dallo stato della versione precedente
                        List<JavaFileEntity> prevVersionJavaFiles= prevVersion.getJavaFiles();

                        for (JavaFileEntity k : prevVersionJavaFiles) {
                            long prevReleaseTime = sdf.parse(prevVersion.getDate()).getTime();
                            double newAge =k.getAge() + ((dateRelease.getTime() -
                                    prevReleaseTime) / (double) 604800000);
                            //<-- Assumo che sia presente anche per tutta la release successiva, se non è cosi viene eliminta
                            //da una DELETE nella release
                            newClass = new JavaFileEntity(k.getClassName(),commitVersion.getVersion(),k.getSize(),
                                    newAge,k.getAuthors());
                            commitVersion.addJavaFile(newClass);
                        }
                    }

                    double age = (dateRelease.getTime() - date.getTime()) / (double)604800000;
                    for (DiffEntry diff : dfs) {
                        if (diff.getOldPath().endsWith(JAVA_FILES) || diff.getNewPath().endsWith(JAVA_FILES)) {
                            String[] newName = diff.getNewPath().split("/");
                            if (newName[newName.length-1].contains("test") || newName[newName.length-1].contains("Test") ) continue;
                            String fileName;

                            if (DiffEntry.ChangeType.DELETE.equals(diff.getChangeType()))
                                fileName = diff.getOldPath();
                            else fileName = diff.getNewPath();

                            JavaFileEntity javaFile = null;
                            for (JavaFileEntity file : commitVersion.getJavaFiles()) {
                                if (file.getClassName().equals(fileName)) {
                                    javaFile = file;
                                }
                            }

                            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                            df.setRepository(repo);
                            FileHeader fileHeader = df.toFileHeader(diff);
                            int newDelete = 0;
                            int newInsert = 0;
                            int newReplace = 0;

                            for (Edit edit : fileHeader.toEditList()) {
                                if (edit.getType().compareTo(Edit.Type.DELETE) == 0) {
                                    newDelete = newDelete + edit.getLengthA();
                                }
                                if (edit.getType().compareTo(Edit.Type.INSERT) == 0) {
                                    newInsert = newInsert + edit.getLengthB();
                                }
                                if (edit.getType().compareTo(Edit.Type.REPLACE) == 0) {
                                    newReplace = newReplace + edit.getLengthB() - edit.getLengthA();
                                }
                            }

                            if (DiffEntry.ChangeType.ADD.equals(diff.getChangeType()) &&
                                    javaFile == null){
                                //<-- ADD può avere DELETE o REPLACE?
                                int value = newInsert - newDelete + newReplace;
                                int locTouched = newInsert + Math.abs(newReplace) + newDelete;
                                int locAdded =  newInsert;
                                List<String> authors = new ArrayList<>();
                                authors.add(rev.getCommitterIdent().getName());
                                if (newReplace > 0) locAdded = locAdded + newReplace;
                                double weightedAge = age * (newInsert + Math.abs(newReplace) + newDelete);
                                int[] aux = new int[] {value,locTouched,locAdded, dfs.size()};
                                newClass = new JavaFileEntity(fileName,aux, commitVersion.getVersion(),
                                        age,weightedAge,authors);
                                commitVersion.addJavaFile(newClass);
                            }
                            else if (DiffEntry.ChangeType.DELETE.equals(diff.getChangeType())){
                                commitVersion.getJavaFiles().remove(javaFile);
                            }
                            else if (javaFile != null) {
                                //Una revisione di una classe è il numero di volte in cui la classe è stata toccata
                                //da un commit diverso in quella versione
//                                        An edit where beginA == endA && beginB < endB is an insert edit, that is sequence B inserted the elements in region [beginB, endB) at beginA.
//
//                                        An edit where beginA < endA && beginB == endB is a delete edit, that is sequence B has removed the elements between [beginA, endA).
//
//                                        An edit where beginA < endA && beginB < endB is a replace edit, that is sequence B has replaced the range of elements between [beginA, endA) with those found in [beginB, endB).
                                int newNR = javaFile.getNr() + 1;
                                int size = javaFile.getSize()+ newInsert - newDelete + newReplace;
                                int locTouched = javaFile.getLocTouched()
                                        + newDelete + newInsert + Math.abs(newReplace);
                                int locAdded = javaFile.getLocAdded() + newInsert;
                                if (newReplace > 0) locAdded = locAdded + newReplace;
                                int maxLocAdded = Math.max(javaFile.getMaxLocAdded(), locAdded);
                                double avgLocAdded = locAdded / (double) newNR;
                                int churn =  javaFile.getChurn() + newInsert - newDelete + newReplace;
                                int maxChurn =Math.max(javaFile.getMaxChurn(), newInsert - newDelete + newReplace);
                                double avgChurn =churn / (double) newNR;
                                int chgSetSize = javaFile.getChgSetSize() + dfs.size() - 1;
                                int maxChgSet = Math.max(javaFile.getMaxChgSetSize(), dfs.size() - 1);
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
                                javaFile.setWeightedAge(javaFile.getAge()*locTouched);
                            }

                        }
                    }
                    prevVersion = commitVersion;
                }
            }
            for (int i=releaseEntityList.size() - 1; i>=0;i--){
                if (releaseEntityList.get(i).getJavaFiles().isEmpty()) {
                    //<-- non ho commit per quella versione
                    releaseEntityList.remove(i); //rimuovo le versioni non interessate da nessun commit
                }
            }
            int nRows =0;
            Object[] aux;
            for (ReleaseEntity entry : releaseEntityList){
                aux = new Object[]{entry.getVersion(), entry.getJavaFiles().size()};
                Logger.getAnonymousLogger().log(Level.INFO, "Totale classi per versione({0}):{1}",aux);
                nRows = nRows +entry.getJavaFiles().size();
            }
            Logger.getAnonymousLogger().log(Level.INFO,"Totale classi/numero righe tra tutte le releases: {0}",nRows);
        } catch (Exception e) {
            e.printStackTrace();
            RetrieveGitInfo.getGit().close();
        }
    }



    public List<CommitEntity> getCommitEntityList(List<RevCommit> commits, List<ReleaseEntity> releaseEntityList, List<JiraTicketsEntity> jiraTicketsEntityList) throws ParseException {
        //Creo una lista di classi Commit con la versione e se presente ticket Id corrispondente
        List<CommitEntity> commitEntityList = new ArrayList<>();
        boolean isPresent;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String[] strTokenized;
        for (RevCommit rev : commits) {
            Date date = rev.getCommitterIdent().getWhen();
            ReleaseEntity commitVersion =
                    RetrieveJiraInfo.getVersionByDate(releaseEntityList, sdf.format(date));
            isPresent = false;
            for (JiraTicketsEntity ticket : jiraTicketsEntityList) {
                if (rev.getFullMessage().contains(ticket.getKey())) {//<--commit con quel ticket ID
                    strTokenized = ticket.getKey().split("-");
                    int len = strTokenized[strTokenized.length - 1].length();
                    String[] str = rev.getFullMessage().split(REG_EX);
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
