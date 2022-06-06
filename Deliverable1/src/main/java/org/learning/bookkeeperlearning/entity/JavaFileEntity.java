package org.learning.bookkeeperlearning.entity;

import java.util.List;

public class JavaFileEntity {
    private final String className;
    private String version;
    private int nr;
    private int size;
    private int locTouched;
    private int locAdded;
    private int maxLocAdded;
    private double avgLocAdded;
    private int churn;
    private int maxChurn;
    private double avgChurn;
    private int chgSetSize;
    private int maxChgSetSize;
    private double avgChgSetSize;
    private double age;
    private double weightedAge;
    private final List<String> authors;
    private String bugginess = "no";

        public JavaFileEntity(String fileName, String newVersion, int newSize, double age, List<String> newAuthors)
        {
            className = fileName;
            version = newVersion;
            nr = 0 ;
            size = newSize;
            locTouched = 0;
            locAdded = 0;
            maxLocAdded = 0;
            avgLocAdded = 0;
            churn = 0;
            maxChurn = 0;
            avgChurn = 0;
            chgSetSize = 0;
            maxChgSetSize = 0;
            avgChgSetSize = 0;
            this.age = age;
            weightedAge = 0;
            authors = newAuthors;
        }

    public JavaFileEntity(String fileName,int[] values, String newVersion,
                          double age,double newWeightedAge, List<String> newAuthors)
    { //int value,int newLocTouched,int newLocAdded,int newChgSet
        className = fileName;
        version = newVersion;
        nr = 1 ;
        size = values[0];
        locTouched = values[1];
        locAdded = values[2];
        maxLocAdded = values[2];
        avgLocAdded = values[2];
        churn = values[0];
        maxChurn = values[0];
        avgChurn = values[0];
        chgSetSize = values[3];
        maxChgSetSize = values[3];
        avgChgSetSize = values[3];
        this.age = age;
        weightedAge = newWeightedAge;
        authors = newAuthors;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public String getBugginess() {
        return bugginess;
    }

    public double getAge() {
        return age;
    }

    public double getAvgChgSetSize() {
        return avgChgSetSize;
    }

    public double getAvgChurn() {
        return avgChurn;
    }

    public double getAvgLocAdded() {
        return avgLocAdded;
    }

    public double getWeightedAge() {
        return weightedAge;
    }

    public int getChgSetSize() {
        return chgSetSize;
    }

    public int getChurn() {
        return churn;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public int getLocTouched() {
        return locTouched;
    }

    public int getMaxChgSetSize() {
        return maxChgSetSize;
    }

    public int getMaxChurn() {
        return maxChurn;
    }

    public int getMaxLocAdded() {
        return maxLocAdded;
    }

    public int getNr() {
        return nr;
    }

    public int getSize() {
        return size;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getClassName() {
        return className;
    }

    public void addAge(double age) {
        this.age += age;
    }

    public void addAuthors(String author) {
        if (!authors.contains(author)) this.authors.add(author);
    }

    public void setAvgChgSetSize() {
            this.avgChgSetSize = this.chgSetSize / (double) this.nr;
    }

    public void setAvgChurn() {
        this.avgChurn = this.churn / (double) this.nr;
    }

    public void setAvgLocAdded() {
        this.avgLocAdded = this.locAdded / (double) this.nr;
    }

    public void setBugginess(String bugginess) {
        this.bugginess = bugginess;
    }

    public void addChgSetSize(int chgSetSize) {
        this.chgSetSize += chgSetSize;
        this.setMaxChgSetSize(chgSetSize);
        this.setAvgChgSetSize();
    }

    public void addChurn(int churn) {
        this.churn += churn;
        this.setMaxChurn(churn);
        this.setAvgChurn();
    }

    public void addLocAdded(int locAdded) {
        this.locAdded += locAdded;
        this.setMaxLocAdded(locAdded);
        this.setAvgLocAdded();
    }

    public void addLocTouched(int locTouched) {
        this.locTouched += locTouched;
    }

    public void setMaxChgSetSize(int maxChgSetSize) {
        this.maxChgSetSize = Math.max(this.getMaxChgSetSize(), maxChgSetSize);
    }

    public void setMaxChurn(int churn) {
        this.maxChurn = Math.max(this.getMaxChurn(), churn);
    }

    public void setMaxLocAdded(int newLocAdded) {
        this.maxLocAdded = Math.max(this.locAdded, newLocAdded);
    }

    public void addNr(int nr) {
        this.nr += nr;
    }

    public void addSize(int size) {
        this.size += size;
    }

    public void setWeightedAge(double weightedAge) {
        this.weightedAge = weightedAge;
    }

    @Override
    public String toString (){
        return String.format("{%s,%s,%d,%d,%d,%d,%d,%f,%d,%d,%f,%d,%d,%f,%f,%f,%s}",className,version,nr,size,locTouched,locAdded,maxLocAdded,
                avgLocAdded,churn,maxChurn,avgChurn,chgSetSize,maxChgSetSize,avgChgSetSize, age,weightedAge,bugginess);
    }

}
