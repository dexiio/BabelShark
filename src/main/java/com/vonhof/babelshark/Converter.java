package com.vonhof.babelshark;

import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.SharkNode;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public interface Converter<T> {
    public T convertTo(BabelSharkInstance bs,SharkNode node) throws MappingException;
    public SharkNode convertFrom(BabelSharkInstance bs,T value) throws MappingException;
}
