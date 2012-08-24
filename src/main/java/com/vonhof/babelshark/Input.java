package com.vonhof.babelshark;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that wraps inputstream with contentType
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class Input {
    private final InputStream input;
    private final String contentType;
    
    public Input(String input, String contentType) {
        InputStream is;
        try {
            is = new ByteArrayInputStream(input.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Input.class.getName()).log(Level.SEVERE, null, ex);
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
