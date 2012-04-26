package com.vonhof.babelshark;

import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.SharkType;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public interface SharkDeserializer<T> {
    public <U> T deserialize(BabelSharkInstance bs,SharkNode node,SharkType<T,U> type) throws MappingException;
}
