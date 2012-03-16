package com.vonhof.babelshark;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Class that wraps inputstream with contentType
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public class Input {
    private final InputStream input;
    private final String contentType;
    
    public Input(String input, String contentType) {
        this.input = new ByteArrayInputStream(input.getBytes());
        this.contentType = contentType;
    }

    public Input(InputStream input, String contentType) {
        this.input = input;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public InputStream getStream() {
        return input;
    }
}
