package org.learning.bookkeeperlearning.entity;

import java.util.ArrayList;
import java.util.List;

public class JiraTicketsEntity {

    private String key;
    private List<ReleaseEntity> avsJira;
    private List<ReleaseEntity> fvsJira;

    private ReleaseEntity iv;
    private ReleaseEntity ov;
    private ReleaseEntity fv;
    private List<ReleaseEntity> computedAvs = new ArrayList<>();


    private final List<Double> incrementalP = new ArrayList<>();
    private final List<Double> incrementalFvIv = new ArrayList<>();

    public List<ReleaseEntity> getComputedAvs() {
        return computedAvs;
    }

    public void setComputedAvs(List<ReleaseEntity> computedAvs) {
        this.computedAvs = computedAvs;
    }

    public void setIv(ReleaseEntity iv) {
        this.iv = iv;
    }

    public ReleaseEntity getIv() {
        return iv;
    }

    public void addIncrementalMeanFvIV(Double incrementalFvIv) {
        this.incrementalFvIv.add(incrementalFvIv);
    }

    public void addIncrementalP(Double incrementalP) {
        this.incrementalP.add(incrementalP);
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

