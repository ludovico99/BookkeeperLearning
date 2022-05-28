package logic;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

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
