package com.vonhof.babelshark.reflect;

import com.vonhof.babelshark.ReflectUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class ClassInfo<T> {
    private final static Map<Integer,ClassInfo> cache = new HashMap<Integer, ClassInfo>();
    
    public static <T> ClassInfo<T> from(Class<T> type) {
        int hash = hash(type);
        if (cache.containsKey(hash))
            return cache.get(hash);
        ClassInfo classInfo = new ClassInfo(type);
        cache.put(hash, classInfo);
        classInfo.read();
        return classInfo;
    }
    
    public static ClassInfo[] fromAll(Class ... types) {
        ClassInfo[] out = new ClassInfo[types.length];
        for(int i = 0;i < types.length;i++) {
            out[i] = ClassInfo.from(types[i]);
        }
        return out;
    }
    
    public static <T> ClassInfo<T> from(Class<T> type,Type genericType) {
        Type[] genTypes = readGenericTypes(genericType);
        int hash = hash(type,genTypes);
        if (cache.containsKey(hash))
            return cache.get(hash);
        ClassInfo classInfo = new ClassInfo(type,genericType);
        cache.put(hash, classInfo);
        classInfo.read();
        return classInfo;
    }
    
    private static int hash(Class type) {
        return hash(type,new Type[0]);
    }
    private static int hash(Class type,Type[] genericTypes) {
        int hash = 3;
        hash = 71 * hash + (type != null ? type.hashCode() : 0);
        hash = 71 * hash + Arrays.deepHashCode(genericTypes);
        return hash;
    }
    

    private final Class<T> type;
    private final Type[] genericTypes;
    private final Map<String,FieldInfo> fields = new LinkedHashMap<String, FieldInfo>();
    private final List<MethodInfo> methods = new ArrayList<MethodInfo>();
    private final Map<Class<? extends Annotation>,Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
    
    private ClassInfo(Class<T> type) {
        this(type, null);
    }
    
    private ClassInfo(Class<T> type,Type genericType) {
        this.type = type;
        if (genericType != null)
            this.genericTypes = readGenericTypes(genericType);
        else
            this.genericTypes = new Type[0];
        
    }
    private void read() {
        readFields();
        readMethods();
        readAnnotations();
    }
    
    private static Type[] readGenericTypes(Type genType) {
        if(genType instanceof ParameterizedType){
            ParameterizedType aType = (ParameterizedType) genType;
            return aType.getActualTypeArguments();
        }
        return new Type[0];
    }
    
    private void readFields() {
        
        Class clz = type;
        while(true) {
            if (clz == null 
                    || clz.equals(Object.class)
                    || ReflectUtils.isPrimitive(clz))
                break;
            
            Field[] clzFields = clz.getDeclaredFields();
            
            for (int i = 0; i < clzFields.length; i++) {
                FieldInfo field = new FieldInfo(clzFields[i]);
                if (!fields.containsKey(field.getName()))
                    fields.put(field.getName(),field);
            }
            
            clz = clz.getSuperclass();
        }
    }
    
    private void readMethods() {
        Class clz = type;
        while(true) {
            if (clz == null 
                    || clz.equals(Object.class)
                    || ReflectUtils.isPrimitive(clz))
                break;
            
            Method[] clzMethods = clz.getDeclaredMethods();
            
            for (int i = 0; i < clzMethods.length; i++) {
                
                MethodInfo method = new MethodInfo(clzMethods[i]);
                methods.add(method);
            }
            
            clz = clz.getSuperclass();
        }
        
    }
    
    private void readAnnotations() {
        for(Annotation a:type.getAnnotations()) {
            annotations.put(a.annotationType(), a);
        }
    }

    public boolean isInstantiatable() {
        return ReflectUtils.isInstantiatable(type);
    }

    public boolean isCollection() {
        return ReflectUtils.isCollection(type);
    }

    public boolean isMap() {
        return ReflectUtils.isMap(type);
    }

    public boolean isPrimitive() {
        return ReflectUtils.isPrimitive(type);
    }

    public boolean isBean() {
        return ReflectUtils.isBean(type);
    }

    public boolean isMapOrCollection() {
        return ReflectUtils.isMapOrCollection(type);
    }

    public boolean isMappable() {
        return ReflectUtils.isMappable(type);
    }

    public Map<String,FieldInfo> getFields() {
        return Collections.unmodifiableMap(fields);
    }
    
    public FieldInfo getField(String name) {
        return fields.get(name);
    }

    public List<MethodInfo> getMethods() {
        return Collections.unmodifiableList(methods);
    }
    public MethodInfo getMethodByClassParms(String name,Class ... args) {
        ClassInfo[] infoArgs = fromAll(args);
        return getMethod(name, infoArgs);
    }
    
    public MethodInfo getMethod(String name,ClassInfo ... args) {
        for(MethodInfo m:methods) {
            if (m.getName().equalsIgnoreCase(name) && m.hasParmTypes(args)) {
                return m;
            }
        }
        return null;
    }

    public Class<T> getType() {
        return type;
    }

    public Type[] getGenericTypes() {
        return genericTypes;
    }
    
    
    public boolean hasAnnotation(Class<? extends Annotation> aType) {
        return annotations.containsKey(aType);
    }
    public <T extends Annotation> T getAnnotation(Class<T> aType) {
        return (T) annotations.get(aType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClassInfo other = (ClassInfo) obj;
        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        if (!Arrays.deepEquals(this.genericTypes, other.genericTypes)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hash(type, genericTypes);
    }

    public boolean isAssignableFrom(ClassInfo classInfo) {
        return isAssignableFrom(classInfo.type);
    }
    public boolean isAssignableFrom(Class clz) {
        return this.type.isAssignableFrom(clz);
    }

    public boolean isA(Class clz) {
        return this.type.equals(clz) || type.isAssignableFrom(clz);
    }

    public boolean isArray() {
        return type.isArray();
    }

    public boolean inherits(Class clz) {
        return clz.isAssignableFrom(type);
    }

    public T newInstance() throws InstantiationException, IllegalAccessException {
        return type.newInstance();
    }

    public String getName() {
        return type.getName();
    }
    
    public Class getComponentType() {
        return type.getComponentType();
    }
    

    @Override
    public String toString() {
        if (genericTypes.length > 0) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(int i = 0;i < genericTypes.length;i++) {
                if (first)
                    first = false;
                else {
                    sb.append(",");
                }
                sb.append(genericTypes[i].toString());
            }
            return String.format("%s<%s>",type.getName(),sb.toString());
        } else {
            return type.getName();
        }
        
    }

    public boolean isEnum() {
        return type.isEnum();
    }
    
    public T[] getEnumConstants() {
        return type.getEnumConstants();
    }
    
}
