package com.vonhof.babelshark.reflect;

import com.vonhof.babelshark.ReflectUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public final class ClassInfo<T> {

    private final static Map<Integer, ClassInfo> cache = new HashMap<Integer, ClassInfo>();

    public static <T> ClassInfo<T> from(Class<T> type) {
        int hash = hash(type);
        if (cache.containsKey(hash)) {
            return cache.get(hash);
        }
        ClassInfo classInfo = new ClassInfo(type);
        cache.put(hash, classInfo);
        classInfo.read();
        return classInfo;
    }

    public static ClassInfo[] fromAll(Class... types) {
        ClassInfo[] out = new ClassInfo[types.length];
        for (int i = 0; i < types.length; i++) {
            out[i] = ClassInfo.from(types[i]);
        }
        return out;
    }

    public static <T> ClassInfo<T> from(Class<T> type, Type[] genTypes) {
        int hash = hash(type, genTypes);
        if (cache.containsKey(hash)) {
            return cache.get(hash);
        }
        ClassInfo classInfo = new ClassInfo(type, genTypes);
        cache.put(hash, classInfo);
        classInfo.read();
        return classInfo;
    }
    public static <T> ClassInfo<T> from(Class<T> type, Type genericType) {
        Type[] genTypes = readGenericTypes(genericType);
        int hash = hash(type, genTypes);
        if (cache.containsKey(hash)) {
            return cache.get(hash);
        }
        ClassInfo classInfo = new ClassInfo(type, genericType);
        cache.put(hash, classInfo);
        classInfo.read();
        return classInfo;
    }

    private static int hash(Class type) {
        return hash(type, new Type[0]);
    }

    private static int hash(Class type, Type[] genericTypes) {
        int hash = 3;
        hash = 71 * hash + (type != null ? type.hashCode() : 0);
        hash = 71 * hash + Arrays.deepHashCode(genericTypes);
        return hash;
    }
    private final Class<T> type;
    private final Type[] genericTypes;
    private final Map<String, FieldInfo> fields = new LinkedHashMap<String, FieldInfo>();
    private final List<MethodInfo> methods = new ArrayList<MethodInfo>();
    private final Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
    private final Map<TypeVariable, Type> typeVariableMap = new HashMap<TypeVariable, Type>();

    private ClassInfo(Class<T> type) {
        this.type = type;
        this.genericTypes = new Type[0];
    }
    
    private ClassInfo(Class<T> type, Type[] genericTypes) {
        this.type = type;
        this.genericTypes = genericTypes;
    }

    private ClassInfo(Class<T> type, Type genericType) {
        this.type = type;

        if (genericType != null) {
            this.genericTypes = readGenericTypes(genericType);
        } else if (this.type.getGenericSuperclass() != null) {
            this.genericTypes = readGenericTypes(this.type.getGenericSuperclass());
        } else {
            this.genericTypes = new Type[0];
        }
    }

    private void read() {
        try {
            readTypeVariables();
            readFields();
            readMethods();
            readAnnotations();
        } catch(Throwable ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,String.format("Could not read class: %s",type),ex);
        }
    }

    private void readTypeVariables() {

        // interfaces
        readGenericInterfaces(type.getGenericInterfaces(), typeVariableMap);

        // super class
        Type genericType = type.getGenericSuperclass();
        Class clz = type.getSuperclass();
        while (clz != null && !Object.class.equals(clz)) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                addToGenericTypeMap(pt, typeVariableMap);
            }
            readGenericInterfaces(clz.getGenericInterfaces(), typeVariableMap);
            genericType = clz.getGenericSuperclass();
            clz = clz.getSuperclass();
        }

        // enclosing class
        clz = type;
        while (clz.isMemberClass()) {
            genericType = clz.getGenericSuperclass();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                addToGenericTypeMap(pt, typeVariableMap);
            }
            clz = clz.getEnclosingClass();
        }
    }
    
    protected final Type getTypeVariableType(TypeVariable typeVar) {
        return typeVariableMap.get(typeVar);
    }

    private static void readGenericInterfaces(Type[] genericInterfaces, Map<TypeVariable, Type> typeVariableMap) {
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                addToGenericTypeMap(pt, typeVariableMap);
                if (pt.getRawType() instanceof Class) {
                    readGenericInterfaces(
                            ((Class) pt.getRawType()).getGenericInterfaces(), typeVariableMap);
                }
            } else if (genericInterface instanceof Class) {
                readGenericInterfaces(
                        ((Class) genericInterface).getGenericInterfaces(), typeVariableMap);
            }
        }
    }

    private static void addToGenericTypeMap(ParameterizedType type, Map<TypeVariable, Type> typeVariableMap) {
        if (type.getRawType() instanceof Class) {
            Type[] actualTypeArguments = type.getActualTypeArguments();
            TypeVariable[] typeVariables = ((Class) type.getRawType()).getTypeParameters();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                TypeVariable variable = typeVariables[i];
                if (actualTypeArgument instanceof Class) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof GenericArrayType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof ParameterizedType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof TypeVariable) {
                    // We have a type that is parameterized at instantiation time
                    // the nearest match on the bridge method will be the bounded type.
                    TypeVariable typeVariableArgument = (TypeVariable) actualTypeArgument;
                    Type resolvedType = typeVariableMap.get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = extractBoundForTypeVariable(typeVariableArgument);
                    }
                    typeVariableMap.put(variable, resolvedType);
                }
            }
        }
    }

    private static Type extractBoundForTypeVariable(TypeVariable typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Object.class;
        }
        Type bound = bounds[0];
        if (bound instanceof TypeVariable) {
            bound = extractBoundForTypeVariable((TypeVariable) bound);
        }
        return bound;
    }

    protected static Type[] readGenericTypes(Type genType) {
        
        if (genType instanceof ParameterizedType) {
            ParameterizedType aType = (ParameterizedType) genType;
            return aType.getActualTypeArguments();
        }
        if (genType instanceof Class) {
            return new Type[]{genType};
        }
        
        return new Type[0];
    }
    
    protected static Type resolveGenericType(Type genType,ClassInfo owner) {
        if (genType instanceof TypeVariable) {
            Type typeVariableType = owner.getTypeVariableType((TypeVariable)genType);
            if (typeVariableType != null) {
                return typeVariableType;
            }
        }          
        return genType;
    }
    
    protected static Type[] resolveGenericTypes(Type[] genTypes,ClassInfo owner) {
        Type[] out = new Type[genTypes.length];
        for(int i = 0; i < genTypes.length;i++) {
            out[i] = resolveGenericType(genTypes[i],owner);
        }
        return out;
    }
    
    protected static Type[] readGenericTypes(Type genType,ClassInfo owner) {
        Type[] genTypes = readGenericTypes(genType);
        return resolveGenericTypes(genTypes, owner);
    }

    private void readFields() {

        Class clz = type;
        while (true) {
            if (clz == null
                    || clz.equals(Object.class)
                    || ReflectUtils.isSimple(clz)) {
                break;
            }

            Field[] clzFields = clz.getDeclaredFields();

            for (int i = 0; i < clzFields.length; i++) {
                FieldInfo field = new FieldInfo(this,clzFields[i]);
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }

            clz = clz.getSuperclass();
        }
    }

    private void readMethods() {
        Class clz = type;
        while (true) {
            if (clz == null
                    || clz.equals(Object.class)
                    || ReflectUtils.isSimple(clz)) {
                break;
            }

            Method[] clzMethods = clz.getDeclaredMethods();

            for (int i = 0; i < clzMethods.length; i++) {
                clzMethods[i].isBridge();
                MethodInfo method = new MethodInfo(this,clzMethods[i]);
                methods.add(method);
            }

            clz = clz.getSuperclass();
        }

    }

    private void readAnnotations() {
        for (Annotation a : type.getAnnotations()) {
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
        return ReflectUtils.isSimple(type);
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

    public Map<String, FieldInfo> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public FieldInfo getField(String name) {
        return fields.get(name);
    }

    public List<MethodInfo> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public MethodInfo getMethodByClassParms(String name, Class... args) {
        ClassInfo[] infoArgs = fromAll(args);
        return getMethod(name, infoArgs);
    }

    public MethodInfo getMethod(String name, ClassInfo... args) {
        for (MethodInfo m : methods) {
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
        return this.type.equals(clz) || (type.isAssignableFrom(clz) && !type.equals(Object.class));
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
            for (int i = 0; i < genericTypes.length; i++) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(genericTypes[i].toString());
            }
            return String.format("%s<%s>", type.getName(), sb.toString());
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

    public List<MethodInfo> getMethodsByAnnotation(Class<? extends Annotation> annotation) {
        List<MethodInfo> out = new ArrayList<MethodInfo>();
        for (MethodInfo method : getMethods()) {
            if (method.hasAnnotation(annotation)) {
                out.add(method);
            }
        }
        return out;
    }

    public List<FieldInfo> getFieldsByAnnotation(Class<? extends Annotation> annotation) {
        List<FieldInfo> out = new ArrayList<FieldInfo>();
        for (Entry<String, FieldInfo> field : getFields().entrySet()) {
            if (field.getValue().hasAnnotation(annotation)) {
                out.add(field.getValue());
            }
        }
        return out;
    }
}
