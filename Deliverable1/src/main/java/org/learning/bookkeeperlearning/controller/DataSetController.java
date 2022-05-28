package org.learning.bookkeeperlearning.controller;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.learning.bookkeeperlearning.controller.GitController;
import org.learning.bookkeeperlearning.entity.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DataSetController {

    private static final String JAVA_FILES = ".java";

    public DataSetEntity computeDataSet (List<ReleaseEntity> releases, List<JiraTicketsEntity> jiraTicketsEntityList,
                                            List<CommitEntity> commitEntityList){

        getIncrementalProportion(releases,jiraTicketsEntityList);
        computeAvsVersionsForAllTickets(releases,jiraTicketsEntityList);
        resetBugginess(releases);
        getClassesModifiedByACommitWithJiraID(releases,commitEntityList);

        return new DataSetEntity(releases,jiraTicketsEntityList);

    }

    public abstract void writeArffFile(DataSetEntity dataSet) throws IOException;


    protected  void  getIncrementalProportion(List<ReleaseEntity> releases, List<JiraTicketsEntity> jiraTicketsEntityList) {
        int iv=-1;
        int ov;
        int fv;

        List<Double> p = new ArrayList<>();
        List<Double> meanFvIV = new ArrayList<>();
        int count1;
        int count2;
        for (int z =1;z<releases.size();z++) {
            List<ReleaseEntity> aux= releases.subList(0, z + 1);
            count1 =0;
            count2=0;
            p.add(0.0);
            meanFvIV.add(0.0);
            for (JiraTicketsEntity tickets : jiraTicketsEntityList) {
                if (!tickets.getAvsJira().isEmpty()) {//<-- Ho injected version
                    List<ReleaseEntity> affectedVersions = tickets.getAvsJira();
                    List<Integer> indexes = new ArrayList<>();
                    for (ReleaseEntity str : affectedVersions) {//trovare l'earliest AVs
                        for (int j = 0; j<aux.size();j++) {
                            if (aux.get(j).getVersion().contains(str.getVersion())) {
                                indexes.add(j);
                            }
                        }
                    }
                    Collections.sort(indexes);//almeno una delle versioni è in AVs
                    if (!indexes.isEmpty()) iv = indexes.get(0);
                    ov = aux.indexOf(tickets.getOv());
                    fv = aux.indexOf(tickets.getFv());

                    if (fv > iv && iv != -1 && fv != -1) {
                        count2++;
                        meanFvIV.set(z - 1, meanFvIV.get(z - 1) + fv - iv);
                    }
                    if (iv != -1 && ov != -1 && fv != -1 && fv > ov && iv <= ov ) {
                        count1++;
                        p.set(z - 1, p.get(z - 1) + (double) (fv - iv) / (double) (fv - ov));
                    }
                }
            }
            if(count1==0) p.set(z-1,0.0);
            else p.set(z-1,p.get(z-1)/count1);

            if (count2 == 0) meanFvIV.set(z-1,1.0);
            else meanFvIV.set(z-1,meanFvIV.get(z - 1) /count2);
            Object[] log = new Object[] {count1,count2};
            Logger.getAnonymousLogger().log(Level.INFO,"numero di tickets consistenti: [{0},{1}]" ,log);
            log = new Object[] {p.get(z-1),meanFvIV.get(z-1)};
            Logger.getAnonymousLogger().log(Level.INFO,"proportion, meanfv-iv = [{0};{1}]",log);
        }

        for (JiraTicketsEntity tickets : jiraTicketsEntityList){
            tickets.setIncrementalP(p);
            tickets.setIncrementalFvIv(meanFvIV);
        }
        Logger.getAnonymousLogger().log(Level.INFO,"Mean Proportion= {0}" , p.get(p.size() - 1));
        Logger.getAnonymousLogger().log(Level.INFO,"Mean FV - IV = {0}" , meanFvIV.get(meanFvIV.size() - 1));
    }


    protected void getClassesModifiedByACommitWithJiraID(List<ReleaseEntity> releases, List<CommitEntity> log){
        int count2=0;
        try {
            Repository repo = GitController.getGit().getRepository();
            if (log != null && repo != null) {
                for (CommitEntity rev : log) {
                    if (rev.getTicket() != null) {
                        count2++;
                        List<DiffEntry> dfs = GitController.diffCommit(rev.getCommit().getName());
                        for (DiffEntry diff : dfs) {
                            if (diff.getNewPath().endsWith(JAVA_FILES)) {
                                String[] tokens = diff.getNewPath().split("/");
                                if (!tokens[tokens.length - 1].contains("test") && !tokens[tokens.length - 1].contains("Test")) {
                                    for (ReleaseEntity releaseEntity: releases) {
                                        for (JavaFileEntity javaFile :releaseEntity.getJavaFiles()) {
                                            if (javaFile.getClassName().equals(diff.getNewPath()) &&
                                                    rev.getTicket().getAvs().contains(releaseEntity)) {
                                                javaFile.setBugginess("yes");
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }

                }
            }
            Logger.getAnonymousLogger().log(Level.INFO, "Totale commits con ticket ID: {0}",count2);

        } catch (Exception e) {
            e.printStackTrace();
            GitController.getGit().close();
        }
    }

    protected void resetBugginess(List<ReleaseEntity> releases){
        for (ReleaseEntity releaseEntity: releases) {
            for (JavaFileEntity javaFile : releaseEntity.getJavaFiles()) {
                javaFile.setBugginess("no");
            }
        }
    }

    protected abstract void computeAvsVersionsForAllTickets(List<ReleaseEntity> releases, List<JiraTicketsEntity> jiraTicketsEntityList);
}
