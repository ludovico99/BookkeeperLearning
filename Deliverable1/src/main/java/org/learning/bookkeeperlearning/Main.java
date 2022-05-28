package org.learning.bookkeeperlearning;

import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;
import org.learning.bookkeeperlearning.controller.*;
import org.learning.bookkeeperlearning.entity.*;
import org.learning.bookkeeperlearning.utilityclasses.ArffFiles;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {


    public static void main(String[] args) throws IOException, JSONException, ParseException {
        int nRows =0;
        Object[] aux;
        //Il main funge da controller applicativo, da orchestratore. Ha la responsabilità di eseguire nel corretto ordine i tasks.


        /* FASE 1: Retrieve dei commit attravero jgit:
        * 1.1: Crea se non esiste una copia in locale del repository di bookkeeper
        *1.2: Ottengo una lista ordinata di RevCommit
        * */


        Logger logger = Logger.getLogger("Bookkeeper dataset creation log");

        logger.log(Level.INFO,"Retrieving commits from git local repo...");

        GitController.initializeGit();
        List<RevCommit> commits = GitController.getOrderedCommits();


        /* FASE 2: Retrieve di jira tickets:
         * 2.1: Attraverso il primo metodo ottengo tutte gli identificativi (nomi) e le date di tutte le release.
         * Successivamente le ordine in base alla data.
         *2.2:  //Per ogni ticket trovo le affected Versions e fixed Versions specificate in jira.
         * Sono due liste di ReleaseEntities.
         * */

        logger.log(Level.INFO,"Retrieving jira tickets info from jira...");

        JiraController jrController = new JiraController();

        List<ReleaseEntity> releaseEntityList = jrController.getVersionsAndDates();
        List<JiraTicketsEntity> jiraTicketsEntityList = jrController.getVersionsOfBugTickets(releaseEntityList);


        /* FASE 3: Calcolo delle features per ogni coppia (versione,classe):
         * 3.1: Bind dei commits con i tickets individuando i commits contrassegnati da tickets di tipo bug.
         * 3.2: Calcolo delle features data la coppia versione e classe.
         * 3.3: Calcolo alcune statistiche
         * */
        logger.log(Level.INFO,"Binding e features computation...");

        FeaturesController featuresController = new FeaturesController();

        List<CommitEntity> commitEntityList = featuresController.bindCommitsAndTickets(commits,releaseEntityList,jiraTicketsEntityList);
        featuresController.getFeaturesPerVersionsAndClasses(commitEntityList,releaseEntityList);

        logger = Logger.getLogger("Features computation info:");

        for (ReleaseEntity entry : releaseEntityList){
            aux = new Object[]{entry.getVersion(), entry.getJavaFiles().size()};
            logger.log(Level.INFO, "Totale classi per versione({0}):{1}",aux);
            nRows = nRows +entry.getJavaFiles().size();
        }
        logger.log(Level.INFO,"Totale classi/numero righe tra tutte le releases: {0}",nRows);


        logger.log(Level.INFO,"Data set and arff files creation ...");


        DataSetController trainingSetController = new TrainingSetController();

        DataSetEntity trainingSet = trainingSetController.computeDataSet(releaseEntityList,jiraTicketsEntityList,commitEntityList);

        trainingSetController.writeArffFile(trainingSet);


        DataSetController testingSetController = new TestingSetController();

        DataSetEntity testingSet = testingSetController.computeDataSet(releaseEntityList,jiraTicketsEntityList,commitEntityList);

        testingSetController.writeArffFile(testingSet);

        GitController.getGit().close();
    }
}




