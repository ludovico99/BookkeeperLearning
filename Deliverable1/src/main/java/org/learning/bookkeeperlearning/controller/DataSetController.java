package org.learning.bookkeeperlearning.controller;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.learning.bookkeeperlearning.entity.*;
import org.learning.bookkeeperlearning.utilityclasses.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DataSetController {

    private static final Logger logger = Logger.getLogger("Data set computation info:");

    public DataSetEntity computeDataSet(List<ReleaseEntity> releases, List<JiraTicketsEntity> jiraTicketsEntityList,
                                        List<CommitEntity> commitEntityList) {

        getIncrementalProportion(releases, jiraTicketsEntityList);
        computeAvsVersionsForAllTickets(releases, jiraTicketsEntityList);
        resetBugginess(releases);
        getClassesModifiedByACommitWithJiraID(releases, commitEntityList);

        return new DataSetEntity(releases, jiraTicketsEntityList);

    }

    public abstract void writeArffFile(DataSetEntity dataSet) throws IOException;


    protected void getIncrementalProportion(List<ReleaseEntity> releases, List<JiraTicketsEntity> jiraTicketsEntityList) {
        int iv = -1;
        int ov;
        int fv;

        double p;
        double meanFvIV;
        int numberProportion;
        int numberFvIvDiff;

        for (int z = 1; z < releases.size(); z++) {
            // Calcolo proportion da 0 fino alla versione in posizione z
            List<ReleaseEntity> releasesToBeConsidered = releases.subList(0, z + 1);

            numberProportion = 0;
            numberFvIvDiff = 0;
            p = 0.0;
            meanFvIV = 0.0;

            for (JiraTicketsEntity ticket : jiraTicketsEntityList) {
                if (!ticket.getAvsJira().isEmpty()) {//<-- Ho injected version

                    iv = findEarliestAVsJira(ticket, releasesToBeConsidered);

                    ov = releasesToBeConsidered.indexOf(ticket.getOv());
                    fv = releasesToBeConsidered.indexOf(ticket.getFv());

                    if (isMeanFvIvValid(fv, iv)) {
                        numberFvIvDiff++;
                        meanFvIV += fv - iv;
                    }
                    if (isProportionValid(iv, ov, fv)) {
                        numberProportion++;
                        p += (double) (fv - iv) / (double) (fv - ov);
                    }
                }
            }
            double[] res = computeProportionAndFvIvDiff(p, meanFvIV, numberProportion, numberFvIvDiff);
            addChangesToTickets(jiraTicketsEntityList, res[0], res[1]);


        }

    }

    private void addChangesToTickets(List<JiraTicketsEntity> jiraTicketsEntityList, double p, double meanFvIV) {
        // Per semplicità per ogni ticket memorizzo la lista di incremental p e meanFvIv calcolati
        for (JiraTicketsEntity tickets : jiraTicketsEntityList) {
            tickets.addIncrementalP(p);
            tickets.addIncrementalMeanFvIV(meanFvIV);
        }
    }

    private double[] computeProportionAndFvIvDiff(double p, double meanFvIV, int numberProportion, int numberFvIvDiff) {
        // Risolvo i casi estremi in cui i consistency check non sono rispettati

        if (numberProportion == 0) p = 0.0;
        else p = p / numberProportion;

        if (numberFvIvDiff == 0) meanFvIV = 1.0;
        else meanFvIV = meanFvIV / numberFvIvDiff;

        Object[] log = new Object[]{numberProportion, numberFvIvDiff};
        logger.log(Level.INFO, "numero di tickets consistenti: [{0},{1}]", log);
        log = new Object[]{p, meanFvIV};
        logger.log(Level.INFO, "incr. proportion, incr. Mean FV-IV = [{0};{1}]", log);

        return new double[]{p, meanFvIV};
    }

    private boolean isProportionValid(int iv, int ov, int fv) {
        return iv != -1 && ov != -1 && fv != -1 && fv > ov && iv <= ov;
    }

    private boolean isMeanFvIvValid(int fv, int iv) {
        return fv > iv && iv != -1 && fv != -1;
    }

    private int findEarliestAVsJira(JiraTicketsEntity ticket, List<ReleaseEntity> releasesToBeConsidered) {
        // Calcolo la piu piccola Realease tra le affected versions dichiarate nel ticket jira
        // ReleaseToBeConsidered è una lista ordinata delle versioni.
        int iv = -1;
        List<ReleaseEntity> affectedVersions = ticket.getAvsJira();
        for (ReleaseEntity str : affectedVersions) {//trovare l'earliest AVs
            for (int j = 0; j < releasesToBeConsidered.size(); j++) {
                if (releasesToBeConsidered.get(j).getVersion().contains(str.getVersion())) {
                    iv = j;
                    return iv;
                }
            }
        }
        return iv; // Se non non esiste un Av valida ritorna -1
    }


    protected void getClassesModifiedByACommitWithJiraID(List<ReleaseEntity> releases, List<CommitEntity> log) {
        int count2 = 0;
        try {
            Repository repo = GitController.getGit().getRepository();
            if (log != null && repo != null) {
                for (CommitEntity rev : log) {
                    if (rev.getTicket() != null) {
                        count2++;
                        List<DiffEntry> dfs = GitController.diffCommit(rev.getCommit().getName());
                        for (DiffEntry diff : dfs) {
                            setBugginess(diff, releases, rev);
                        }
                    }
                }
            }
            logger.log(Level.INFO, "Totale commits con ticket ID: {0}", count2);

        } catch (Exception e) {
            e.printStackTrace();
            GitController.getGit().close();
        }
    }

    private void setBugginess(DiffEntry diff, List<ReleaseEntity> releases, CommitEntity rev) {
        if (Boolean.TRUE.equals(Utilities.isAJavaFileExcludeTests(diff))) {
            for (ReleaseEntity releaseEntity : releases) {
                for (JavaFileEntity javaFile : releaseEntity.getJavaFiles()) {
                    if (javaFile.getClassName().equals(diff.getNewPath()) &&
                            rev.getTicket().getComputedAvs().contains(releaseEntity)) {
                        javaFile.setBugginess("yes");
                    }
                }
            }
        }
    }

    private void resetBugginess(List<ReleaseEntity> releases) {
        for (ReleaseEntity releaseEntity : releases) {
            for (JavaFileEntity javaFile : releaseEntity.getJavaFiles()) {
                javaFile.setBugginess("no");
            }
        }
    }

    public abstract void computeAvsVersionsForAllTickets(List<ReleaseEntity> releases, List<JiraTicketsEntity> jiraTicketsEntityList);


    private int computeIvWithProportion(int fv, int ov, JiraTicketsEntity ticket, int index) {
        int iv = -1;
        if (fv > ov)
            iv = (int) (fv - (fv - ov) * ticket.getIncrementalP().get(index));
        else iv = (int) (fv - ticket.getIncrementalFvIv().get(index)); //<-- ov inconsistente
        return iv;
    }

    protected void computeIv( JiraTicketsEntity ticket, List<ReleaseEntity> releases, int index) {
        int iv;

        int fv = releases.indexOf(ticket.getFv());
        int ov = releases.indexOf(ticket.getOv());

        List<ReleaseEntity> computedAvs;

        if (ticket.getAvsJira().isEmpty()) {
            iv = computeIvWithProportion(fv, ov, ticket, index);
        } else {
            iv = findEarliestAVsJira(ticket, releases);
            if (iv != -1) { //almeno una delle versioni è in AVs di Jira
                if (iv >= fv || iv >= ov) //iv trovato ma inconsistente
                {
                    iv = computeIvWithProportion(fv, ov, ticket, index);
                }
            } else { //<-- non ho trovato la earliest released Av tra le AVs di Jira
                iv = computeIvWithProportion(fv, ov, ticket, index);
            }
        }
        if (iv < 0) iv = 0; // ho sottratto mean fv-iv ed è maggiore di fv
        computedAvs = new ArrayList<>(releases.subList(iv, fv));
        ticket.setIv(releases.get(iv));
        ticket.setComputedAvs(computedAvs);

    }
}
