package org.learning.bookkeeperlearning.utility;

import org.eclipse.jgit.diff.DiffEntry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.learning.bookkeeperlearning.entity.ReleaseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    private Utilities () {}

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

    public static boolean isTicketExactlyContained(String source, String ticketId){
        String pattern = "\\b" + ticketId + "\\b";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(source);
        return m.find();
    }

    public static Boolean isAJavaFileExcludeTests (DiffEntry diff) {
        String javaSuffix = ".java";
        String name = diff.getNewPath();
        if (name.contains("test")) return false;

        return diff.getOldPath().endsWith(javaSuffix) || diff.getNewPath().endsWith(javaSuffix);

    }


}
