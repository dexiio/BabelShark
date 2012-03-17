package com.vonhof.babelshark;

import com.vonhof.babelshark.annotation.FactoryMethod;
import com.vonhof.babelshark.annotation.Name;
import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.ObjectNode;
import com.vonhof.babelshark.node.SharkType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MappedBean<T> {
    private final Map<String,ObjectField> fields = new LinkedHashMap<String, ObjectField>();
    private final Class<T> clz;
    private final Method factoryMethod;

    public MappedBean(Class<T> clz) throws MappingException {
        this.clz = clz;
        FactoryMethod annotation = clz.getAnnotation(FactoryMethod.class);
        if (annotation != null) {
            try {
                factoryMethod = clz.getMethod(annotation.value(), ObjectNode.class);
            } catch (Exception ex) {
                throw new MappingException(String.format("Could not find provided factory method for class: %s",clz), ex);
            }
            if (!Modifier.isStatic(factoryMethod.getModifiers()) 
                    || !Modifier.isPublic(factoryMethod.getModifiers()))
                throw new MappingException(String.format("Provided factory method must be public static for class: %s",clz));
            if (factoryMethod.getParameterTypes().length != 1 
                    || factoryMethod.getParameterTypes()[0].equals(ObjectNode.class))
                throw new MappingException(String.format("Provided factory method must have exactly one argument of type ObjectNode for class: %s",clz));
        } else {
            factoryMethod = null;
        }
    }

    public boolean hasFactoryMethod() {
        return factoryMethod != null;
    }

    public Map<String, ObjectField> getFields() {
        return fields;
    }
    public Set<String> getFieldList() {
        return fields.keySet();
    }

    public ObjectField addField(String name,Field field,Method getter,Method setter) {
        final ObjectField oField = new ObjectField(field, getter, setter);
        this.fields.put(name,oField);
        return oField;
    }
    
    public ObjectField getField(String name) {
        return this.fields.get(name);
    }

    
    public T newInstance(ObjectNode o) throws MappingException {
        try {
            if (factoryMethod != null)
                return (T) factoryMethod.invoke(null,o);
            return clz.newInstance();
        } catch (Exception ex) {
            throw new MappingException(ex);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MappedBean<T> other = (MappedBean<T>) obj;
        if (this.fields != other.fields && (this.fields == null || !this.fields.equals(other.fields))) {
            return false;
        }
        if (this.clz != other.clz && (this.clz == null || !this.clz.equals(other.clz))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.fields != null ? this.fields.hashCode() : 0);
        hash = 67 * hash + (this.clz != null ? this.clz.hashCode() : 0);
        return hash;
    }
    
    public class ObjectField {
        private final Field field;
        private final SharkType type;
        private Method getter;
        private Method setter;


        public ObjectField(Field field, Method getter, Method setter) {
            this.field = field;
            
            this.getter = getter;
            this.setter = setter;
            
            Name annotation = field.getAnnotation(Name.class);
            if (annotation != null 
                    && annotation.generics().length > 0) {
                this.type = SharkType.get(field);
                return;
            }
            
            if (ReflectUtils.isMapOrCollection(field.getType())) {
                //Attempt to get the list or map value types
                if (getter != null) {
                    type = SharkType.get(field.getType(),getter.getGenericReturnType());
                    return;
                }
                if (setter != null && getter.getGenericParameterTypes().length > 0) {
                    type = SharkType.get(field.getType(),getter.getGenericParameterTypes()[0]);
                    return;
                }
                if (Modifier.isPublic(field.getModifiers())) {
                    type = SharkType.get(field.getType(),field.getGenericType());
                    return;
                }
            }
            this.type = SharkType.get(field);
        }
        

        public SharkType getType() {
            return type;
        }
        

        public void setGetter(Method getter) {
            this.getter = getter;
        }

        public void setSetter(Method setter) {
            this.setter = setter;
        }
        
        public Object get(T obj) throws MappingException {
            try {
                if (getter == null)
                    return field.get(obj);
                return getter.invoke(obj);
            } catch (Exception ex) {
                throw new MappingException(ex);
            }
        }
        public void set(T obj,Object value) throws MappingException {
            try {
                if (setter == null)
                    field.set(obj, value);
                else
                    setter.invoke(obj, value);
            } catch (Exception ex) {
                throw new MappingException(ex);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ObjectField other = (ObjectField) obj;
            if (this.field != other.field && (this.field == null || !this.field.equals(other.field))) {
                return false;
            }
            if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
                return false;
            }
            if (this.getter != other.getter && (this.getter == null || !this.getter.equals(other.getter))) {
                return false;
            }
            if (this.setter != other.setter && (this.setter == null || !this.setter.equals(other.setter))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + (this.field != null ? this.field.hashCode() : 0);
            hash = 23 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 23 * hash + (this.getter != null ? this.getter.hashCode() : 0);
            hash = 23 * hash + (this.setter != null ? this.setter.hashCode() : 0);
            return hash;
        }
    }
}
