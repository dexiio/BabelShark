package com.vonhof.babelshark.impl;

import com.vonhof.babelshark.BeanMapper;
import com.vonhof.babelshark.MappedBean;
import com.vonhof.babelshark.ReflectionUtils;
import com.vonhof.babelshark.annotation.Ignore;
import com.vonhof.babelshark.annotation.Name;
import com.vonhof.babelshark.exception.MappingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DefaultBeanMapper implements BeanMapper {
    private final Map<Class,MappedBean> cache = new HashMap<Class, MappedBean>();

    public <T> MappedBean<T> getMap(Class<T> type) throws MappingException {
        //Get cached version
        MappedBean obj = cache.get(type);
        if (obj != null)
            return obj;
        
        //Not a bean
        if (!ReflectionUtils.isBean(type))
            throw new MappingException(String.format("Cannot get bean map for class %s. Not a bean!",type.getName()));
        
        if (!ReflectionUtils.isInstantiatable(type)) {
            throw new MappingException(String.format("Cannot instantiate bean: %s",type.getName()));
        }
        
        
        
        obj = new MappedBean(type);
        
        
        for(Field field:type.getDeclaredFields()) {
            if (ignoreField(type, field)) 
                continue;
            final String fieldName = getFieldName(type, field);
            obj.addField(fieldName, field, 
                    getGetter(type, field), 
                    getSetter(type, field));
        }
        
        cache.put(type,obj);
        
        return obj;
    }
    
    protected boolean ignoreField(Class type,Field field) {
        Ignore anno = field.getAnnotation(Ignore.class);
        return (anno != null);
    }
    
    protected String getFieldName(Class type,Field field) {
        String name = field.getName();
        Name nameAnno = field.getAnnotation(Name.class);
        if (nameAnno != null)
            name = nameAnno.value();
        return name;
    }
    private String ucFirst(String name) {
        return name.substring(0,1).toUpperCase().concat(name.substring(1));
    }
    protected Method getGetter(Class type,Field field) {
        String name = ucFirst(field.getName());
        String getterName = (Boolean.class.equals(type)) ? "is"+name : "get"+name;
        try {
            return type.getDeclaredMethod(getterName);
        } catch (Exception ex) {
            return null;
        }
    }
    
    protected Method getSetter(Class type,Field field) {
        String name = ucFirst(field.getName());
        String setterName = "set"+name;
        try {
            return type.getMethod(setterName,field.getType());
        } catch (Exception ex) {
            return null;
        }
    }

}
