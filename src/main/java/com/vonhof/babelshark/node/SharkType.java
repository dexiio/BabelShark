package com.vonhof.babelshark.node;

import com.vonhof.babelshark.ReflectUtils;
import com.vonhof.babelshark.annotation.Name;
import com.vonhof.babelshark.reflect.ClassInfo;
import com.vonhof.babelshark.reflect.FieldInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public final class SharkType<T,U> {

    private final Class<T> type;
    private final SharkType<U,?> valueType;
    private final boolean collection;
    private final boolean map;

    private SharkType(Class type,boolean collection, boolean map,SharkType valueType) {
        this.type = type;
        this.valueType = valueType;
        this.collection = collection;
        this.map = map;
    }

    public boolean isCollection() {
        return collection;
    }

    public boolean isMap() {
        return map;
    }

    
    public SharkType<U,?> getValueType() {
        return valueType;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        if (map)
            return String.format("%s<String,%s>",type.getName(),valueType);
        if (collection)
            return String.format("%s<%s>",type.getName(),valueType);
        return type.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SharkType<T, U> other = (SharkType<T, U>) obj;
        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        if (this.valueType != other.valueType && (this.valueType == null || !this.valueType.equals(other.valueType))) {
            return false;
        }
        if (this.collection != other.collection) {
            return false;
        }
        if (this.map != other.map) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 71 * hash + (this.valueType != null ? this.valueType.hashCode() : 0);
        hash = 71 * hash + (this.collection ? 1 : 0);
        hash = 71 * hash + (this.map ? 1 : 0);
        return hash;
    }
    
    public static <T,U> SharkType<T,U> forCollection(Class<T> type,SharkType<U,?> entryType) {
        return new SharkType(type, true, false, entryType);
    }
    public static <T,U> SharkType<T,U> forCollection(Class<T> type,Class<U> entryType) {
        return (SharkType<T, U>) forCollection(type, get(entryType));
    }
    
    public static <T,U> SharkType<T,U> forMap(Class<T> type,SharkType<U,?> entryType) {
        return new SharkType(type, false, true, entryType);
    }
    public static <T,U> SharkType<T,U> forMap(Class<T> type,Class<U> entryType) {
        return (SharkType<T, U>) forMap(type, get(entryType));
    }
    public static <T> SharkType<T,?> forSimple(Class<T> type) {
        return new SharkType<T,Object>(type, false, false, null);
    }
    
    public static <T,U> SharkType<T,?> get(Class<T> type) {
        return get(type,Object.class);
    }
    public static <T,U> SharkType<T,U> get(Class<T> type,Class<U> valueType) {
        if (ReflectUtils.isMap(type)) {
            return forMap(type, valueType);
        }
        
        if (ReflectUtils.isCollection(type)) {
            return forCollection(type, valueType);
        }
        
        return (SharkType<T, U>) forSimple(type);
    }
    
    public static SharkType get(FieldInfo field) {
        ClassInfo type = field.getType();
        return get(type);
    }
    
    public static <T> SharkType<T,?> get(ClassInfo<T> info) {
        Class valueType = Object.class;
        if (info.isMap() && info.getGenericTypes().length > 1) {
            valueType = (Class) info.getGenericTypes()[1];
        }
        if (info.isCollection() && info.getGenericTypes().length > 0) {
            valueType = (Class) info.getGenericTypes()[0];
        }
        return get(info.getType(),valueType);
    }
    
}
