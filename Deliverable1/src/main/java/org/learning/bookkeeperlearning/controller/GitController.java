package org.learning.bookkeeperlearning.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitController {
    private static Git git;
    private static Repository repo;

    private static final Logger logger = Logger.getLogger("Git controller log");


    private static final String REPOSITORY = "https://github.com/apache/bookkeeper.git";
    private static final String PROJECT_NAME = "Bookkeeper";

    private GitController(){

    }

    private static final String LOCAL_GIT_REPO_PREFIX = ".\\";
    public static Git getGit() {
        return git;
    }

    public static void initializeGit() {
        try {
                git = Git.cloneRepository()
                        .setURI(REPOSITORY)
                        .setDirectory(new File(LOCAL_GIT_REPO_PREFIX + PROJECT_NAME))
                        .call();

        } catch (Exception e) {
                logger.log(Level.SEVERE,"Error in cloning the repository" ,e);
                try {
                    git = Git.open(new File(LOCAL_GIT_REPO_PREFIX + PROJECT_NAME));
                } catch (Exception ev) {
                    logger.log(Level.SEVERE,"Error in opening local repo" ,e);
                    if (git != null) git.close();
                }
            }

    }


    public static List<RevCommit> getOrderedCommits () {
        List<RevCommit> results;
        try {

            Iterable<RevCommit> commits = git.log().all().call();
            results = new ArrayList<>();
            for (RevCommit commit : commits) {
                results.add(0, commit);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error in retrieving commits" ,e);
            return Collections.emptyList();
        }
        return results;
    }


    public static List<DiffEntry> diffCommit(String hashID) throws IOException {
        //Initialize repositories.
        repo = git.getRepository();

        //Get the commit you are looking for.
        RevCommit newCommit;
        try (RevWalk walk = new RevWalk(repo)) {
            newCommit = walk.parseCommit(repo.resolve(hashID));
        }

        return getDiffOfCommit(newCommit);

    }

    public static String verify(String hashID) throws IOException {

        //Get the commit you are looking for.
        RevCommit newCommit;
        try (RevWalk walk = new RevWalk(repo)) {
            newCommit = walk.parseCommit(repo.resolve(hashID));
        }

        //Get commit that is previous to the current one.
        RevCommit oldCommit = getPrevHash(newCommit);
        if(oldCommit == null){
            return null;
        }
        //Use treeIterator to diff.
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);
        OutputStream outputStream = new ByteArrayOutputStream();
        try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
            formatter.setRepository(repo);
            formatter.format(oldTreeIterator, newTreeIterator);
        } catch (Exception e){
            logger.log(Level.SEVERE,"Error in DiffFormatter method");
        }
        Logger.getAnonymousLogger().log(Level.INFO,"LogCommit: {}", newCommit);
        String logMessage = newCommit.getFullMessage();
        Logger.getAnonymousLogger().log(Level.INFO,"LogMessage: {}", logMessage);
        //Print diff of the commit with the previous one.
        return outputStream.toString();
    }

    //Helper gets the diff as a string.
    private static List<DiffEntry> getDiffOfCommit(RevCommit newCommit) throws IOException {

        //Get commit that is previous to the current one.
        RevCommit oldCommit = getPrevHash(newCommit);
        List<DiffEntry> diffs = null;
        if(oldCommit == null){
            return Collections.emptyList();
        }
        //Use treeIterator to diff.
        AbstractTreeIterator oldTreeIterator = getCanonicalTreeParser(oldCommit);
        AbstractTreeIterator newTreeIterator = getCanonicalTreeParser(newCommit);
        try (DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            formatter.setRepository(repo);
            formatter.setDiffComparator(RawTextComparator.DEFAULT);
            diffs = formatter.scan(oldTreeIterator,newTreeIterator);
        } catch (Exception e){
            logger.log(Level.SEVERE,"Error in diff scanning" ,e);
        }
        return diffs;
    }
    //Helper function to get the previous commit.
    private static RevCommit getPrevHash(RevCommit commit)  throws  IOException {

        try (RevWalk walk = new RevWalk(repo)) {
            // Starting point
            walk.markStart(commit);
            int count = 0;
            for (RevCommit rev : walk) {
                // got the previous commit.
                if (count == 1) {
                    return rev;
                }
                count++;
            }
            walk.dispose();
        }
        //Reached end and no previous commits.
        return null;
    }
    //Helper function to get the tree of the changes in a commit. Written by Rüdiger Herrmann
    private static AbstractTreeIterator getCanonicalTreeParser(ObjectId commitId) throws IOException {
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit commit = walk.parseCommit(commitId);
            ObjectId treeId = commit.getTree().getId();
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                return new CanonicalTreeParser(null, reader, treeId);
            }
        }
    }
}
