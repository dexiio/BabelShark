package com.vonhof.babelshark.node;

import com.vonhof.babelshark.ReflectUtils;
import com.vonhof.babelshark.reflect.ClassInfo;
import com.vonhof.babelshark.reflect.FieldInfo;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public final class SharkType<T,U> {

    private final Class<T> type;
    private final SharkType<U,?> valueType;
    private final boolean collection;
    private final boolean map;
    private Boolean array = null;

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
    public boolean isPrimitive() {
        return ReflectUtils.isSimple(type);
    }
    
    public boolean inherits(Class clz) {
        return clz.isAssignableFrom(type);
    }
    
    public boolean isArray() {
        if (array == null)
            return type.isArray();
        return array;
    }

    private void setArray(Boolean array) {
        this.array = array;
    }
    
    
    public boolean isA(Class clz) {
        return this.type.equals(clz);
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
    
    public static <T,U> SharkType<T,U> get(Class<T> type,Class<U> valueType) {
        return get(type,SharkType.get(valueType));
    }
    
    public static <T,U> SharkType<T,?> get(Class<T> type) {
        return get(ClassInfo.from(type));
    }
    
    public static SharkType get(FieldInfo field) {
        return get(field.getType());
    }
    
    
    public static <T,U> SharkType<T,U> get(Class<T> type,SharkType<U,?> valueType) {
        if (ReflectUtils.isMap(type)) {
            return forMap(type, valueType);
        }
        
        if (ReflectUtils.isCollection(type)) {
            return forCollection(type, valueType);
        }
        
        return (SharkType<T, U>) new SharkType<T, U>(type,false,false,valueType);
    }
    
    
    
    public static <T> SharkType<T,?> get(ClassInfo<T> info) {
        SharkType valueType = new SharkType(Object.class, false,false,null);
        if (info.isMap() && info.getGenericTypes().length > 1) {
            valueType = type2Class(info.getGenericTypes()[1]);
            
        }
        if (info.isCollection() && info.getGenericTypes().length > 0) {
            valueType = type2Class(info.getGenericTypes()[0]);
        }
        
        if (info.isArray()) {
            valueType = type2Class(info.getComponentType());
        }
        return get(info.getType(),valueType);
    }
    
    private static SharkType type2Class(Type type) {
        if (type instanceof ParameterizedType) {
            final ParameterizedType subType = (ParameterizedType) type;
            final Type[] typeArgs = subType.getActualTypeArguments();
            if (ReflectUtils.isMap(subType.getRawType())) {
                if (typeArgs.length > 1) 
                    return forMap((Class)subType.getRawType(),type2Class(typeArgs[1]));
                else
                    return forMap((Class)subType.getRawType(),Object.class);
            }
            if (ReflectUtils.isCollection(subType.getRawType())) {
                if (typeArgs.length > 0)
                    return forCollection((Class)subType.getRawType(),type2Class(typeArgs[0]));
                else
                    return forCollection((Class)subType.getRawType(),Object.class);
            }
            return type2Class(subType.getRawType());
        }
        
        if (type instanceof TypeVariable) {
            return SharkType.get(Object.class);
        }
        
        if (type instanceof GenericArrayType) {
            SharkType out = type2Class(((GenericArrayType)type).getGenericComponentType());
            out.setArray(true);
            return out;
        }
        
        return SharkType.get((Class) type);
    }
    
}
