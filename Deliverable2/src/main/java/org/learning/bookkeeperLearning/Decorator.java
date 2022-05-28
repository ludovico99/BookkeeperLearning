package org.learning.bookkeeperLearning;

public abstract class Decorator extends Validation {

    private Validation validation;

    protected Decorator(String dataSetName,Validation validation) {
        super(validation.getTrainingSet(),validation.getTestingSet(),dataSetName);
        this.validation = validation;
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }


}
