package com.vonhof.babelshark;

import com.vonhof.babelshark.node.SharkNode;
import java.io.IOException;

/**
 *
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public interface ObjectWriter {
    /**
     * Get content type of output (mime type)
     * @return 
     */
    public String getContentType();
    
    /**
     * Write output
     * @param out
     * @param node 
     */
    public void write(Output out,SharkNode node) throws IOException;
}
