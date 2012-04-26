package com.vonhof.babelshark;

import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.SharkNode;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public interface SharkSerializer<T> {
    public SharkNode serialize(BabelSharkInstance bs,T value) throws MappingException;
}
