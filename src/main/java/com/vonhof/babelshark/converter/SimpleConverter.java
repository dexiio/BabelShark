package com.vonhof.babelshark.converter;

import com.vonhof.babelshark.BabelSharkInstance;
import com.vonhof.babelshark.ConvertUtils;
import com.vonhof.babelshark.SharkConverter;
import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.SharkType;
import com.vonhof.babelshark.node.ValueNode;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class SimpleConverter implements SharkConverter<Object> {

    public SharkNode serialize(BabelSharkInstance bs, Object value) throws MappingException {
        return new ValueNode(value);
    }

    public <U> Object deserialize(BabelSharkInstance bs, SharkNode node, SharkType<Object,U> type) throws MappingException {
        if (!node.is(SharkNode.NodeType.VALUE))
            throw new MappingException(String.format("Could not convert %s to %s",node,type));
        
        Class<Object> clz = type.getType();
        
        ValueNode valueNode = (ValueNode) node;
        Object o = valueNode.getValue();
        
        if (clz.equals(Object.class) && o != null)
            clz = (Class<Object>) o.getClass();
        
        if (clz.isInstance(o))
            return o;
        
        if (clz.equals(String.class)) {
            if (o == null) 
                return null;
            return String.valueOf(o);
        }
        
        if (o instanceof String) {
            try {
                return ConvertUtils.convert((String)o, clz);
            } catch(RuntimeException ex) {
                throw new MappingException(ex);
            }
        }
        if (o instanceof Number) {
            Number number = (Number) o;
            return ConvertUtils.convert(number, clz);
        }
        
        if (clz.equals(Boolean.TYPE) 
                    || Boolean.class.equals(clz)) {
            if (o == null)
                return false;
            return o;
        }
        
        if (clz.equals(Character.TYPE) 
                || Character.class.equals(clz)) {
            if (o == null)
                return (char)0;
            return o;
        }
        if (clz.equals(Byte.TYPE) 
                || Byte.class.equals(clz)) {
            if (o == null)
                return (byte)0;
            return o;
        }
        if (clz.equals(Short.TYPE) 
                || Short.class.equals(clz)) {
            if (o == null)
                return (short)0;
            return o;
        }
        if (clz.equals(Integer.TYPE) 
                || Integer.class.equals(clz)) {
            if (o == null)
                return 0;
            return o;
        }
        if (clz.equals(Long.TYPE) 
                || Long.class.equals(clz)) {
            if (o == null)
                return 0L;
            return o;
        }
        if (clz.equals(Float.TYPE) 
                || Float.class.equals(clz)) {
            if (o == null)
                return 0F;
            return o;
        }
        if (clz.equals(Double.TYPE) 
                || Double.class.equals(clz)) {
            if (o == null)
                return 0D;
            return o;
        }
        if (o == null)
            return null;
        throw new MappingException(String.format("Could not convert %s to %s",o,clz.getName()));
    }

}
