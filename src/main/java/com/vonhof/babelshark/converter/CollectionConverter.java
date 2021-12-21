package com.vonhof.babelshark.converter;

import com.vonhof.babelshark.BabelSharkInstance;
import com.vonhof.babelshark.ReflectUtils;
import com.vonhof.babelshark.SharkConverter;
import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.ArrayNode;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.SharkType;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.util.*;


/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class CollectionConverter implements SharkConverter<Object> {


    public SharkNode serialize(BabelSharkInstance bs, Object instance) throws MappingException {
        ArrayNode node = new ArrayNode();
        if (instance.getClass().isArray()) {
            int length = Array.getLength(instance);
            for (int i = 0; i < length; i++) {
                Object value = Array.get(instance, i);
                node.add(bs.write(value));
            }
        } else {
            Collection list = (Collection) instance;
            for (Object value : list) {
                node.add(bs.write(value));
            }
        }

        return node;
    }

    public <U> Object deserialize(BabelSharkInstance bs, SharkNode node, SharkType<Object, U> type) throws MappingException {
        if (!node.is(SharkNode.NodeType.LIST)) {
            bs.reportError(String.format("Could not convert %s to %s", node, type));
            return null;
        }

        ArrayNode listNode = (ArrayNode) node;

        Collection out = null;
        try {
            Class clz = type.getType();
            if (clz.isArray()) {
                Object array = Array.newInstance(clz.getComponentType(), listNode.size());
                for (int i = 0; i < listNode.size(); i++) {
                    Object value = bs.convert(listNode.get(i), clz.getComponentType());
                    Array.set(array, i, value);
                }
                return array;
            } else if (!ReflectUtils.isInstantiatable(clz)) {
                if (Set.class.isAssignableFrom(clz)) {
                    clz = HashSet.class;
                } else if (List.class.isAssignableFrom(clz)) {
                    clz = ArrayList.class;
                } else if (Collection.class.isAssignableFrom(clz)) {
                    clz = ArrayList.class;
                } else {
                    bs.reportError(String.format("Unknown collection type: %s", type));
                    return null;
                }
            }

            out = (Collection) clz.newInstance();
        } catch (Exception ex) {
            throw new MappingException(ex);
        }

        for (SharkNode childNode : listNode) {
            out.add(bs.read(childNode, type.getValueType()));
        }
        return out;
    }
}
