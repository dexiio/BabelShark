package com.vonhof.babelshark.converter;

import com.vonhof.babelshark.BabelSharkInstance;
import com.vonhof.babelshark.BeanMap;
import com.vonhof.babelshark.BeanMapper;
import com.vonhof.babelshark.SharkSerializer;
import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkNode;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class BeanMapConverter implements SharkSerializer<BeanMap> {
    
    private final BeanConverter bc;
    private final MapConverter mc;

    public BeanMapConverter(BeanConverter bc, MapConverter mc) {
        this.bc = bc;
        this.mc = mc;
    }

    public BeanMapConverter() {
        this(new BeanConverter(),new MapConverter());
    }
    
    public BeanMapConverter(BeanMapper bm) {
        this(new BeanConverter(bm),new MapConverter());
    }
    


    @Override
    public SharkNode serialize(BabelSharkInstance bs, BeanMap value) throws MappingException {
        ObjectNode out = new ObjectNode();
        if (value.getBean() != null) {
            out = (ObjectNode) bc.serialize(bs, value.getBean());
            
        }
        ObjectNode map = (ObjectNode) mc.serialize(bs, value);
        for(String field:map.getFields()) {
            out.put(field,map.get(field));
        }
        
        return out;
    }
    
    
    
}
