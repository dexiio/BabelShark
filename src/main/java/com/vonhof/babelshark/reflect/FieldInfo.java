package com.vonhof.babelshark.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class FieldInfo {
    private final Field field;
    private final ClassInfo type;
    private final Map<Class<? extends Annotation>,Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();

    public FieldInfo(Field field) {
        this.field = field;
        this.type = ClassInfo.from(this.field.getType(),this.field.getGenericType());
        readAnnotations();
    }
    
    private void readAnnotations() {
        for(Annotation a:field.getDeclaredAnnotations()) {
            annotations.put(a.annotationType(), a);
        }
    }

    public String getName() {
        return field.getName();
    }
    
    public boolean hasAnnotation(Class<? extends Annotation> aType) {
        return annotations.containsKey(aType);
    }
    public <T extends Annotation> T getAnnotation(Class<T> aType) {
        return (T) annotations.get(aType);
    }

    public Field getField() {
        return field;
    }

    public ClassInfo getType() {
        return type;
    }

    public boolean isPublic() {
        return Modifier.isPublic(field.getModifiers());
    }
    
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }
    
    
    public Object get(Object instance) throws IllegalArgumentException, IllegalAccessException {
        return field.get(instance);
    }
    
    public void set(Object instance,Object value) throws IllegalArgumentException, IllegalAccessException  {
        field.set(instance,value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldInfo other = (FieldInfo) obj;
        if (this.field != other.field && (this.field == null || !this.field.equals(other.field))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.field != null ? this.field.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("%s::%s",field.getDeclaringClass().getName(),getName());
    }

    public void forceAccessible() {
        field.setAccessible(true);
    }
    
   
}
