package com.vonhof.babelshark.converter;

import com.vonhof.babelshark.BabelSharkInstance;
import com.vonhof.babelshark.ReflectUtils;
import com.vonhof.babelshark.SharkConverter;
import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.SharkType;
import com.vonhof.babelshark.node.ValueNode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class MapConverter implements SharkConverter<Map> {
    
    @Override
    public <U> Map deserialize(BabelSharkInstance bs, SharkNode node, SharkType<Map, U> type) throws MappingException {
        if (!node.is(SharkNode.NodeType.MAP)) {
            //Ignore null or empty string values
            if (node.is(SharkNode.NodeType.VALUE) && 
                    ( ((ValueNode)node).getValue() == null || "".equals(String.valueOf(((ValueNode)node).getValue())))) 
                return null;
            bs.reportError(String.format("Could not convert %s to %s",node,type));
            return null;
        }
            
        ObjectNode objNode = (ObjectNode) node;
        Map<String,U> out = null;
        try {
            
            Class clz = type.getType();
            if (!ReflectUtils.isInstantiatable(clz)) {
                if (Map.class.isAssignableFrom(clz) || Object.class.equals(clz)) {
                    clz = LinkedHashMap.class;
                } else {
                    bs.reportError(String.format("Unknown map type: %s",type));
                    return null;
                }
            }
            
            out = (Map<String, U>) clz.newInstance();
        } catch (Exception ex) {
            throw new MappingException(ex);
        }
        
        for(String field:objNode.getFields()) {
            out.put(field, bs.read(objNode.get(field),type.getValueType()));
        }
        return out;
    }

    @Override
    public SharkNode serialize(BabelSharkInstance bs, Map instance) throws MappingException {
        ObjectNode out = new ObjectNode(); 
        Map<Object,Object> map = (Map<Object,Object>)instance;
        for (Map.Entry<Object,Object> entry:map.entrySet()) {
            out.put(String.valueOf(entry.getKey()),bs.write(entry.getValue()));
        }
        return out;
    }

}
