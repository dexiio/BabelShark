package com.vonhof.babelshark.converter;

import com.vonhof.babelshark.BabelSharkInstance;
import com.vonhof.babelshark.SharkConverter;
import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.SharkType;
import com.vonhof.babelshark.node.ValueNode;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class EnumConverter implements SharkConverter<Enum> {

    public <U> Enum deserialize(BabelSharkInstance bs, SharkNode node, SharkType<Enum, U> type) throws MappingException {
        if (!node.is(SharkNode.NodeType.VALUE))
            throw new MappingException("Invalid Enum value");
        ValueNode val = (ValueNode) node;
        if (val.getValue() == null)
            return null;
        if (val.getValue() instanceof String)
            return Enum.valueOf(type.getType(),(String)val.getValue());
        if (val.getValue() instanceof Enum)
            return (Enum) val.getValue();
        
        return null;
        
    }

    public SharkNode serialize(BabelSharkInstance bs, Enum value) throws MappingException {
        return new ValueNode<String>(value.toString());
    }

}
