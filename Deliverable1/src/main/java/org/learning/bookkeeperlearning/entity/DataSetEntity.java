package org.learning.bookkeeperlearning.entity;

import java.util.List;

public class DataSetEntity {

    private final List<ReleaseEntity> releases;
    private final List<JiraTicketsEntity> jiraTicketsEntityList;

    public DataSetEntity(List<ReleaseEntity> releases, List<JiraTicketsEntity> jiraTicketsEntityList) {
        this.releases = releases;
        this.jiraTicketsEntityList = jiraTicketsEntityList;
    }

    public List<ReleaseEntity> getReleases() {
        return releases;
    }

    public List<JiraTicketsEntity> getJiraTicketsEntityList() {
        return jiraTicketsEntityList;
    }
}
