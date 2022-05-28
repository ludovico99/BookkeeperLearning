package org.learning.bookkeeperLearning;

import java.util.List;

public class JiraTicketsEntity {

    private String key;
    private List<ReleaseEntity> avsJira;
    private List<ReleaseEntity> avs;
    private List<ReleaseEntity> fvsJira;
    private ReleaseEntity ov;
    private ReleaseEntity fv;
    private ReleaseEntity iv;


    private List<Double> incrementalP;
    private List<Double> incrementalFvIv;

    public List<ReleaseEntity> getAvs() {
        return avs;
    }

    public void setAvs(List<ReleaseEntity> avs) {
        this.avs = avs;
    }

    public void setIv(ReleaseEntity iv) {
        this.iv = iv;
    }

    public ReleaseEntity getIv() {
        return iv;
    }

    public void setIncrementalFvIv(List<Double> incrementalFvIv) {
        this.incrementalFvIv = incrementalFvIv;
    }

    public void setIncrementalP(List<Double> incrementalP) {
        this.incrementalP = incrementalP;
    }

    public List<Double> getIncrementalFvIv() {
        return incrementalFvIv;
    }

    public List<Double> getIncrementalP() {
        return incrementalP;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ReleaseEntity getFv() {
        return fv;
    }

    public ReleaseEntity getOv() {
        return ov;
    }

    public List<ReleaseEntity> getAvsJira() {
        return avsJira;
    }

    public void setAvsJira(List<ReleaseEntity> avsJira) {
        this.avsJira = avsJira;
    }

    public void setFv(ReleaseEntity fv) {
        this.fv = fv;
    }

    public void setOv(ReleaseEntity ov) {
        this.ov = ov;
    }

    public List<ReleaseEntity> getFvsJira() {
        return fvsJira;
    }

    public void setFvsJira(List<ReleaseEntity> fvsJira) {
        this.fvsJira = fvsJira;
    }

    @Override
    public String toString (){
        return String.format("[TicketID:%s, [OV:%s,FV:%s], [AVs:%s], [FVs:%s]",key, ov,fv, avsJira.toString(), fvsJira.toString() );
    }
}

