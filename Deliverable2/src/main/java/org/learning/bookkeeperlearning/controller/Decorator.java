package org.learning.bookkeeperlearning.controller;

public abstract class Decorator extends Validation {

    private Validation validation;

    protected Decorator(String dataSetName,Validation val) {
        super(val.validationEntity.getTrainingSet(),val.validationEntity.getTestingSet(),dataSetName);
        this.validation = val;
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }


}
