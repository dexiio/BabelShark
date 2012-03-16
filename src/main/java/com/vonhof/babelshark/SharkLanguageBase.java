package com.vonhof.babelshark;

/**
 * A convenience base class for implementing BabelShark languages
 * @author Henrik Hofmeister <@vonhofdk>
 */
abstract public class SharkLanguageBase implements SharkLanguage{
    private final String id;
    private final String[] contentTypes;

    protected SharkLanguageBase(String id, String ... contentTypes) {
        this.id = id;
        this.contentTypes = contentTypes;
    }
    
    public String getId() {
        return id;
    }

    public String[] getContentTypes() {
        return contentTypes;
    }
}
