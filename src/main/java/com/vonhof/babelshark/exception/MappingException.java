package com.vonhof.babelshark.exception;

public class MappingException extends Exception {

    public MappingException() {
        
    }
   
    public MappingException(String string) {
        super(string);
    }

    public MappingException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
    
    public MappingException(Throwable thrwbl) {
        super(thrwbl);
    }

}
