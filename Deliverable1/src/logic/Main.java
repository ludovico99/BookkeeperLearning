package logic;

import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONException;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class Main {


    public static void main(String[] args) throws IOException, JSONException, ParseException {
        //Il main funge da controller applicativo, da orchestratore. Ha la responsabilità di eseguire nel corretto ordine i tasks
        RetrieveFeatures features = new RetrieveFeatures();
        RetrieveGitInfo.initializeGit();
        List<ReleaseEntity> releaseEntityList = RetrieveJiraInfo.getVersionsAndDates();
        List<JiraTicketsEntity> jiraTicketsEntityList = RetrieveJiraInfo.getVersionsOfBugTickets(releaseEntityList); //<--per ogni ticket trovo affectedVersions e fixed Versions
        List<RevCommit> commits = RetrieveGitInfo.getOrderedCommits();
        List<CommitEntity> commitEntityList = features.getCommitEntityList(commits,releaseEntityList,jiraTicketsEntityList);
        features.getFeaturesPerVersionsAndClasses(commitEntityList,releaseEntityList);

        DataSet trainingSet = new TrainingSet(releaseEntityList,jiraTicketsEntityList);

        trainingSet.getIncrementalProportion();
        trainingSet.computeAvsVersionsForAllTickets();

        //commitEntityList è uguale per entrambe le classi
        trainingSet.getClassesModifiedByACommitWithJiraID(commitEntityList);


        ArffFiles.addLinesToDataSet(trainingSet);
        ArffFiles.getWriterTraining().close();

        DataSet testingSet  = new TestingSet(releaseEntityList,jiraTicketsEntityList);

        testingSet.computeAvsVersionsForAllTickets();

        testingSet.resetBugginess();
        testingSet.getClassesModifiedByACommitWithJiraID(commitEntityList);

        ArffFiles.addLinesToDataSet(testingSet);
        ArffFiles.getWriterTesting().close();

        RetrieveGitInfo.getGit().close();
    }
}




