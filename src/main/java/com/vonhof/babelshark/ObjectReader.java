package com.vonhof.babelshark;

import com.vonhof.babelshark.node.SharkNode;
import java.io.IOException;

/**
 *
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public interface ObjectReader {
    /**
     * Get supported content types for input (mime types)
     * @return 
     */
    public String[] getContentTypes();
    /**
     * Read input
     * @param out
     * @return 
     */
    public SharkNode read(Input out) throws IOException;
}
