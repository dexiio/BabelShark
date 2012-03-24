package com.vonhof.babelshark.impl;

import com.vonhof.babelshark.BeanMapper;
import com.vonhof.babelshark.MappedBean;
import com.vonhof.babelshark.annotation.Ignore;
import com.vonhof.babelshark.annotation.Name;
import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.reflect.ClassInfo;
import com.vonhof.babelshark.reflect.FieldInfo;
import com.vonhof.babelshark.reflect.MethodInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultBeanMapper implements BeanMapper {
    private final Map<ClassInfo,MappedBean> cache = new HashMap<ClassInfo, MappedBean>();

    public <T> MappedBean<T> getMap(Class<T> type) throws MappingException {
        return getMap(ClassInfo.from(type));
    }
    public <T> MappedBean<T> getMap(ClassInfo<T> type) throws MappingException {
        //Get cached version
        MappedBean obj = cache.get(type);
        if (obj != null)
            return obj;
        
        //Not a bean
        if (!type.isBean())
            throw new MappingException(String.format("Cannot get bean map for class %s. Not a bean!",type.getName()));
        
        if (!type.isInstantiatable()) {
            throw new MappingException(String.format("Cannot instantiate bean: %s",type.getName()));
        }
        
        obj = new MappedBean(type);
        
        
        
        for(Entry<String,FieldInfo> entry:type.getFields().entrySet()) {
            FieldInfo f = entry.getValue();
            if (ignoreField(f)) 
                continue;
            
            final String fieldName = getFieldName(f);
            MethodInfo getter = getGetter(type, f);
            MethodInfo setter = getSetter(type, f);
            obj.addField(fieldName, f,getter,setter);
        }
        
        cache.put(type,obj);
        
        return obj;
    }
    
    protected boolean ignoreField(FieldInfo field) {
        return field.hasAnnotation(Ignore.class);
    }
    
    protected String getFieldName(FieldInfo field) {
        String name = field.getName();
        Name nameAnno = field.getAnnotation(Name.class);
        if (nameAnno != null)
            name = nameAnno.value();
        return name;
    }
    private String ucFirst(String name) {
        return name.substring(0,1).toUpperCase().concat(name.substring(1));
    }
    protected MethodInfo getGetter(ClassInfo type,FieldInfo field) {
        String name = ucFirst(field.getName());
        String getterName = (type.isA(Boolean.class)) ? "is"+name : "get"+name;
        try {
            return type.getMethod(getterName);
        } catch (Exception ex) {
            return null;
        }
    }
    
    protected <T> MethodInfo getSetter(ClassInfo<T> type,FieldInfo field) {
        String name = ucFirst(field.getName());
        String setterName = "set"+name;
        try {
            return type.getMethod(setterName,field.getType());
        } catch (Exception ex) {
            return null;
        }
    }

}
