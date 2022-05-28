package org.learning.bookkeeperlearning.entity;

import org.eclipse.jgit.revwalk.RevCommit;


public class CommitEntity {
    private RevCommit commit;
    private ReleaseEntity version;
    private JiraTicketsEntity ticket = null;

    public CommitEntity (RevCommit commit, ReleaseEntity version){
        this.commit = commit;
        this.version = version;
    }

    public CommitEntity (RevCommit commit, ReleaseEntity version, JiraTicketsEntity ticket){
        this.commit = commit;
        this.version = version;
        this.ticket = ticket;
    }

    public void setVersion(ReleaseEntity version) {
        this.version = version;
    }

    public RevCommit getCommit() {
        return commit;
    }

    public void setCommit(RevCommit commit) {
        this.commit = commit;
    }

    public JiraTicketsEntity getTicket() {
        return ticket;
    }


    public ReleaseEntity getVersion() {
        return version;
    }

    public void setTicket(JiraTicketsEntity ticket) {
        this.ticket = ticket;
    }
    @Override
    public String toString (){
        return String.format("{%s,%s} = %s",commit.getName(),ticket,version);
    }

}
