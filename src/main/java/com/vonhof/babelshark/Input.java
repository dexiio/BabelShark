package com.vonhof.babelshark;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * Class that wraps inputstream with contentType
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class Input {
    private final static Logger log = LogManager.getLogger(Input.class);
    private final InputStream input;
    private final String contentType;
    
    public Input(String input, String contentType) {
        InputStream is;
        try {
            is = new ByteArrayInputStream(input.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            log.error("Failed to ready input", ex);
            is = new ByteArrayInputStream(input.getBytes());
        }
        this.input = is;
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
