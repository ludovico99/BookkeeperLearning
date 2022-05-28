package org.learning.bookkeeperLearning;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RetrieveJiraInfo {

    private static final String PROJECT_NAME = "BOOKKEEPER";
    //STORM
    //BOOKKEEPER

    private RetrieveJiraInfo (){

    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONArray(jsonText);
        }
    }

    private  static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    public static List<JiraTicketsEntity> getVersionsOfBugTickets(List<ReleaseEntity> releaseEntityList) throws IOException, ParseException {

        String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + PROJECT_NAME + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,fixVersions,resolutiondate,versions,created&startAt=" + 1;


        JSONObject json1 = readJsonFromUrl(url);
        JSONArray issues;
        int total = json1.getInt("total");
        final String field = "fields";
        List<JiraTicketsEntity> result = new ArrayList<>();
        List<ReleaseEntity> aux1;
        List<ReleaseEntity> aux2;
        int i = 0;
        do {
//            https://issues.apache.org/jira/rest/api/2/search?jql=project=%22Storm%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR%20%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22
            url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" + PROJECT_NAME + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
                    + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22ORDER%20BY%20\"created\"%20ASC&fields=key,fixVersions,resolutiondate,versions,created&startAt="
                    + i + "&maxResults=" + 50;
            json1 = readJsonFromUrl(url);
            issues = json1.getJSONArray("issues");
            for (int j = 0; j < issues.length(); j++) {
                JiraTicketsEntity tickets = new JiraTicketsEntity();
                JSONObject element = issues.getJSONObject(j);
                tickets.setKey(element.get("key").toString());
                Object created = element.getJSONObject(field).get("created");
                Object resolutionDate = element.getJSONObject(field).get("resolutiondate");
                if ( created != null)
                    tickets.setOv(getVersionByDate(releaseEntityList, created.toString().substring(0,10)));
                else tickets.setOv(null);
                if (resolutionDate != null)  tickets.setFv(getVersionByDate(releaseEntityList, resolutionDate.toString().substring(0,10)));
                else tickets.setFv(null);

                aux1 = getVersionsLists(element,"versions",releaseEntityList);
                aux2 = getVersionsLists(element,"fixVersions",releaseEntityList);

                tickets.setAvsJira(aux1);
                tickets.setFvsJira(aux2);
                result.add(tickets);
            }

            i = i + 50;
        } while (i < total);
        Logger.getAnonymousLogger().log(Level.INFO,"numero di tickets: {0}",result.size());
        return result;
    }

    public static List<ReleaseEntity> getVersionsLists(JSONObject element, String key, List<ReleaseEntity> releaseEntityList){
        // Ottieni tutte le versioni/fixed versions per ogni ticket Jira
        final String field = "fields";
        List<ReleaseEntity> aux1 = new ArrayList<>();
        ReleaseEntity toAdd;
        JSONArray ar1 = element.getJSONObject(field).getJSONArray(key);
        if (ar1.length() > 0) {
            for (int z = 0; z < ar1.length(); z++) {
                toAdd = getVersionByName(releaseEntityList,ar1.getJSONObject(z).get("name").toString());
                if (toAdd != null) aux1.add(toAdd);
            }
        }
      return aux1;
    }

    public static ReleaseEntity getVersionByDate(List<ReleaseEntity> allVersions, String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateToCompare = new Date(sdf.parse(date).getTime());
        for (ReleaseEntity allVersion : allVersions) {
            Date dateVersion = new Date(sdf.parse(allVersion.getDate()).getTime());
            if (dateVersion.after(dateToCompare)) {
                return allVersion;
            }
        }

        return null; //<-- l'ultima versione ha 0 classi/elementi. Per questo prendo half releases piu uno.
    }

    public static ReleaseEntity getVersionByName(List<ReleaseEntity> allVersions, String name) {
        for (ReleaseEntity allVersion : allVersions) {
           if (allVersion.getVersion().equals(name))
                return allVersion;
            }
        return null;
    }




    public static List<ReleaseEntity> getVersionsAndDates() throws IOException {

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + PROJECT_NAME + "/versions";
        JSONArray json = readJsonArrayFromUrl(url);
        List<ReleaseEntity> list = new ArrayList<>();
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

        Logger.getAnonymousLogger().log(Level.INFO,"Numero di versioni released considerate: {0}",list.size());
        Logger.getAnonymousLogger().log(Level.INFO,"Totale versioni (released/unreleased): {0}",total);
        Logger.getAnonymousLogger().log(Level.INFO,"Versioni: {0}",list);
        return list;
    }
}
