package org.learning.bookkeeperlearning.exceptions;

public class JsonParsingException extends  Exception{
    private static final long serialVersionUID = 1L;
    private static final String ERRORMSG = "Impossibile fare il parsing del JSON: ";

    public JsonParsingException (){
        super(ERRORMSG);
    }

    public JsonParsingException (String str,Throwable cause) {
        super(" +++ " + ERRORMSG + str + " +++ ", cause);
    }
}
