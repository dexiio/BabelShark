package com.vonhof.babelshark;

import java.lang.reflect.*;
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
    public static boolean isCollection(Class type) {
        return (Collection.class.isAssignableFrom(type) || type.isArray());
    }
    
    public static boolean isMap(Class type) {
        return (Map.class.isAssignableFrom(type));
    }
    
    public static boolean isMapOrCollection(Class type) {
        return isMap(type) || isCollection(type);
    }
    
    public static boolean isMappable(Class type) {
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
    public static boolean isInstantiatable(Class type) {
         return !type.isInterface() 
                 && !Modifier.isAbstract( type.getModifiers()) 
                 && !type.isAnnotation()
                 && !type.isArray()
                 && !type.isPrimitive()
                 && !type.equals(Object.class);
    }
    
    public static boolean isBean(Class type) {
        return !isCollection(type) && !isMap(type) && !isSimple(type);
    }
    
}
