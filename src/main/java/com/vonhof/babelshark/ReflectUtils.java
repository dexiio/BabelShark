package com.vonhof.babelshark;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

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
    
    public static boolean isPrimitive(Class type) {
        return type.isPrimitive() 
                || type.equals(String.class) 
                || type.equals(Class.class) 
                || type.equals(Date.class)
                || type.equals(Timestamp.class)
                || type.equals(Time.class)
                || type.equals(Boolean.class) 
                || type.equals(Integer.class) 
                || type.equals(Float.class) 
                || type.equals(Double.class) 
                || type.equals(Long.class) 
                || type.equals(boolean.class) 
                || type.equals(int.class) 
                || type.equals(float.class) 
                || type.equals(double.class) 
                || type.equals(long.class) 
                || type.equals(BigDecimal.class) 
                || type.equals(BigInteger.class) 
                || Enum.class.isAssignableFrom(type);
    }
    public static boolean isInstantiatable(Class type) {
         return !type.isInterface() 
                 && !Modifier.isAbstract( type.getModifiers()) 
                 && !type.isAnnotation()
                 && !type.isArray()
                 && !type.isPrimitive()
                 && !type.equals(Object.class)
                 && !Enum.class.isAssignableFrom(type);
    }
    
    public static boolean isBean(Class type) {
        return !isCollection(type) && !isMap(type) && !isPrimitive(type);
    }
    
    public static Type[] getGenericReturnType(Method method) {
        Type type = method.getGenericReturnType();
        return getGenericType(type);
    }
    
    public static Type[] getGenericParmTypes(Method method) {
        Type[] types = method.getGenericParameterTypes();

        for(Type type : types){
            if(type instanceof ParameterizedType){
                return getGenericType(type);
            }
        }
        return new Type[0];
    }
    public static Type[] getGenericFieldTypes(Field field) {
        Type type = field.getGenericType();
        return getGenericType(type);
        
    }
    public static Type[] getGenericType(Type type) {
        if(type instanceof ParameterizedType){
            ParameterizedType aType = (ParameterizedType) type;
            return aType.getActualTypeArguments();
        }
        return new Type[0];
    }
    
}
