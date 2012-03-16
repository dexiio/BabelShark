package com.vonhof.babelshark;

/**
 * Interface usable to provide a single instance that provides all the information needed for adding a language / format 
 * to BabelShark
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public interface SharkLanguage {
    public String getId();
    public String[] getContentTypes();
    public ObjectReader getObjectReader();
    public ObjectWriter getObjectWriter();
}
