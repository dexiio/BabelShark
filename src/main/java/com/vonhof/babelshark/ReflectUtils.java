package com.vonhof.babelshark;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class ReflectUtils {
    public static boolean isCollection(Type type) {
        if (!(type instanceof Class)) return false;
        Class clz = (Class) type;
        return (Collection.class.isAssignableFrom(clz) || clz.isArray());
    }
    
    public static boolean isMap(Type type) {
        if (!(type instanceof Class)) return false;
        return (Map.class.isAssignableFrom((Class)type));
    }
    
    public static boolean isMapOrCollection(Type type) {
        if (!(type instanceof Class)) return false;
        return isMap(type) || isCollection((Class)type);
    }
    
    public static boolean isMappable(Type type) {
        if (!(type instanceof Class)) return false;
        return isMap(type) || isBean(type);
    }
    
    public static boolean isSimple(Class type) {
        return type.isPrimitive() 
                    || isSimple((Type)type)
                    || Enum.class.isAssignableFrom(type)
                    || Calendar.class.isAssignableFrom(type);
        
        
    }
    public static boolean isSimple(Type type) {
        return type.equals(String.class) 
                || type.equals(Class.class) 
                || type.equals(Date.class)
                || type.equals(Timestamp.class)
                || type.equals(Time.class)
                || type.equals(Boolean.class) 
                || type.equals(Boolean.TYPE)
                || type.equals(Character.class) 
                || type.equals(Character.TYPE) 
                || type.equals(Byte.class) 
                || type.equals(Byte.TYPE) 
                || type.equals(Short.class) 
                || type.equals(Short.TYPE) 
                || type.equals(Integer.class) 
                || type.equals(Integer.TYPE) 
                || type.equals(Float.class) 
                || type.equals(Float.TYPE) 
                || type.equals(Double.class) 
                || type.equals(Double.TYPE) 
                || type.equals(Long.class) 
                || type.equals(Long.TYPE)
                || type.equals(UUID.class)
                || type.equals(BigDecimal.class) 
                || type.equals(BigInteger.class);
    }
    public static boolean isInstantiatable(Type type) {
        if (!(type instanceof Class)) return false;
        Class clz = (Class) type;
        return !clz.isInterface() 
                 && !Modifier.isAbstract( clz.getModifiers()) 
                 && !clz.isAnnotation()
                 && !clz.isArray()
                 && !clz.isPrimitive()
                 && !clz.equals(Object.class);
    }
    
    public static boolean isBean(Type type) {
        if (!(type instanceof Class)) return false;
        return !isCollection(type) && !isMap(type) && !isSimple(type);
    }
    
}
