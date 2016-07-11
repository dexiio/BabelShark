package com.vonhof.babelshark.converter;


public class BabelSharkException extends RuntimeException {

    public BabelSharkException(String format, Throwable ex) {
        super(format, ex);
    }
}
