package com.vonhof.babelshark;

import com.vonhof.babelshark.node.SharkType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class TypeRegistry<T> {
    
    private Map<SharkType,T> registry = new HashMap<SharkType, T>();
    
    public void put(SharkType type,T value) {
        registry.put(type, value);
    }
    public T get(SharkType type) {
        T value = registry.get(type);
        if (value != null)
            return value;
        //No direct values found - look for interfaces etc.
        Class clz = type.getType();
        if (clz.isArray()) {
            type = SharkType.forCollection(Collection.class, clz.getComponentType());
            return get(type);
        }
        if ((type.isCollection() || type.isMap()) 
                && !type.getValueType().getType().equals(Object.class)) {
            return get(SharkType.get(type.getType()));
        }
        
        //Check super classes
        while(value == null) {
            if (clz.equals(Object.class))
                break;
            
            //Check interfaces
            for(Class iface:clz.getInterfaces()) {
                value = registry.get(SharkType.get(iface));
                if (value != null) return value;
            }
            
            Class superClz = clz.getSuperclass();
            if (superClz != null)
                value = registry.get(SharkType.get(superClz));
            
            clz = superClz;
        }

        return value;
    }

}
