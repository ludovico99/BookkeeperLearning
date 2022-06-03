package org.learning.bookkeeperlearning.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.learning.bookkeeperlearning.entity.JiraTicketsEntity;
import org.learning.bookkeeperlearning.entity.ReleaseEntity;
import org.learning.bookkeeperlearning.exceptions.JsonParsingException;
import org.learning.bookkeeperlearning.utilityclasses.JsonParsing;
import org.learning.bookkeeperlearning.utilityclasses.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JiraController {

    private static final String PROJECT_NAME = "BOOKKEEPER";
    private static final Logger logger = Logger.getLogger("Jira tickets info:");


    public  List<JiraTicketsEntity> getVersionsOfBugTickets(List<ReleaseEntity> releaseEntityList){

        String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + PROJECT_NAME + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,fixVersions,resolutiondate,versions,created&startAt=" + 1;
        List<JiraTicketsEntity> result = null;
        try {
            JSONObject json1 = JsonParsing.readJsonFromUrl(url);
            JSONArray issues;
            int total = json1.getInt("total");
            final String field = "fields";
            result = new ArrayList<>();
            List<ReleaseEntity> aux1;
            List<ReleaseEntity> aux2;
            int i = 0;
            do {
                //            https://issues.apache.org/jira/rest/api/2/search?jql=project=%22Storm%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR%20%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22
                url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + PROJECT_NAME + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                        + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22ORDER%20BY%20\"created\"%20ASC&fields=key,fixVersions,resolutiondate,versions,created&startAt="
                        + i + "&maxResults=" + 50;
                json1 = JsonParsing.readJsonFromUrl(url);
                issues = json1.getJSONArray("issues");
                for (int j = 0; j < issues.length(); j++) {
                    JiraTicketsEntity tickets = new JiraTicketsEntity();
                    JSONObject element = issues.getJSONObject(j);
                    tickets.setKey(element.get("key").toString());
                    Object created = element.getJSONObject(field).get("created");
                    Object resolutionDate = element.getJSONObject(field).get("resolutiondate");
                    if (created != null)
                        tickets.setOv(Utilities.getVersionByDate(releaseEntityList, created.toString().substring(0, 10)));
                    else tickets.setOv(null);
                    if (resolutionDate != null)
                        tickets.setFv(Utilities.getVersionByDate(releaseEntityList, resolutionDate.toString().substring(0, 10)));
                    else tickets.setFv(null);

                    aux1 = Utilities.getVersionsLists(element, "versions", releaseEntityList);
                    aux2 = Utilities.getVersionsLists(element, "fixVersions", releaseEntityList);

                    tickets.setAvsJira(aux1);
                    tickets.setFvsJira(aux2);
                    result.add(tickets);
                }

                i = i + 50;
            } while (i < total);
            Logger.getAnonymousLogger().log(Level.INFO, "numero di tickets: {0}", result.size());
        }catch(Exception e){
            JsonParsingException exception= new JsonParsingException("Errore nel retrieve dei tickets",e);
            logger.log(Level.SEVERE,"Error in date parsing" ,exception);
        }
        return result;
    }

    public  List<ReleaseEntity> getVersionsAndDates(){
        List<ReleaseEntity> list = null;
        try {
            String url = "https://issues.apache.org/jira/rest/api/2/project/" + PROJECT_NAME + "/versions";
            JSONArray json = JsonParsing.readJsonArrayFromUrl(url);
            list = new ArrayList<>();
            final String releaseDate = "releaseDate";
            int total = json.length();
            for (int j = 0; j < total; j++) {
                JSONObject element = json.getJSONObject(j);
                if (element.has(releaseDate)) {
                    ReleaseEntity versionsAndDates = new ReleaseEntity(element.get("name").toString(), element.get(releaseDate).toString());
                    if (!list.contains(versionsAndDates)) {
                        list.add(versionsAndDates);
                    }
                }
            }
            list.sort(Comparator.comparing(ReleaseEntity::getDate));


            logger.log(Level.INFO, "Numero di versioni released considerate: {0}", list.size());
            logger.log(Level.INFO, "Totale versioni (released/unreleased): {0}", total);
            logger.log(Level.INFO, "Versioni: {0}", list);

        }catch (IOException e){
            JsonParsingException exception= new JsonParsingException("Errore nel retrieve delle version",e);
            logger.log(Level.SEVERE,"Error in reading JSON Array from URL" ,exception);
        }
        return list;
    }
}


