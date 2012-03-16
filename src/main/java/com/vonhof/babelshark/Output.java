package com.vonhof.babelshark;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Class that wraps outputstream with contentType
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public class Output {
    private final OutputStream output;
    private final String contentType;
    
    public Output(String contentType) {
        this(new ByteArrayOutputStream(),contentType);
    }

    public Output(OutputStream output, String contentType) {
        this.output = output;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public OutputStream getStream() {
        return output;
    }
}
