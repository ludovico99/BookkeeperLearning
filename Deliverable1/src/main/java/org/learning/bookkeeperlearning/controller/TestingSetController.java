package org.learning.bookkeeperlearning.controller;

import org.learning.bookkeeperlearning.entity.DataSetEntity;
import org.learning.bookkeeperlearning.entity.JiraTicketsEntity;
import org.learning.bookkeeperlearning.entity.ReleaseEntity;
import org.learning.bookkeeperlearning.utilityclasses.ArffFiles;

import java.io.IOException;
import java.util.List;

public class TestingSetController extends DataSetController {


    @Override
    public void writeArffFile(DataSetEntity dataSet) throws IOException {
        ArffFiles.addLinesToDataSet(dataSet,false);
        ArffFiles.getWriterTesting().close();
    }

    @Override
    public void computeAvsVersionsForAllTickets(List<ReleaseEntity> releases, List<JiraTicketsEntity> jiraTicketsEntityList) {
        for (JiraTicketsEntity ticket : jiraTicketsEntityList) {
            //Calcolo prima l'AVs per ticketId e poi trovo le classi
            //modificate da commit con quel ticketID
            if (ticket.getFv() != null) {
                int index = ticket.getIncrementalP().size() - 1;

                computeIv(ticket,releases,index);

            }
        }
    }

}
