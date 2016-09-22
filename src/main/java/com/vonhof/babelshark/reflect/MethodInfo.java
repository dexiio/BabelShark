package com.vonhof.babelshark.reflect;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.Paranamer;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;


/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class MethodInfo {

    private static final Paranamer paranamer = new AdaptiveParanamer();
    
    private final ClassInfo owner;
    private final Method method;
    private final ClassInfo returnType;
    private final Map<Class<? extends Annotation>,Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();
    private final Map<String,Parameter> parameters = new LinkedHashMap<String, Parameter>();
    private final List<Method> inheritance = new LinkedList<Method>();

    MethodInfo(ClassInfo owner,Method method) {
        this.owner = owner;
        this.method = method;
        
        Class tmpReturnType = method.getReturnType();
        Type genType = method.getGenericReturnType();
        
        if (genType instanceof TypeVariable) {
            Type typeVariableType = ClassInfo.resolveGenericType(genType, owner);
            if (typeVariableType instanceof Class) {
                tmpReturnType = (Class) typeVariableType;
            }            
        }
        
        if (genType instanceof ParameterizedType) {
            returnType = ClassInfo.from(tmpReturnType,ClassInfo.readGenericTypes(genType, owner));
        } else {
            returnType = ClassInfo.from(tmpReturnType,genType);
        }
        
        readInheritance();
        
        readParameters();
        
        readAnnotations();
    }
    
    private void readInheritance() {
        Method m = method;
        inheritance.add(m);
        Class<?>[] parms = method.getParameterTypes();
        parentTraverse:
        while(m != null) {
            Class<?> parent = m.getDeclaringClass().getSuperclass();
            
            if (parent == null) 
                break;
            
            m = null;
            
            //Look for overridden method
            
            lookupLoop:
            for(Method pM:parent.getDeclaredMethods()) {
                //Names must match
                if (!pM.getName().equals(method.getName())) 
                    continue;
                //Parms must match
                if (pM.getParameterTypes().length != parms.length)
                    continue;
                
                //Return type must match
                if (!pM.getReturnType().isAssignableFrom(method.getReturnType()))
                    continue;
                
                //Compare parameters
                Class<?>[] pParms = pM.getParameterTypes();
                for(int i = 0; i < parms.length;i++) {
                    if (pParms[i].equals(parms[i])
                            || pParms[i].isAssignableFrom(parms[i]))
                        continue;
                    
                    continue lookupLoop;
                }
                m = pM;
                inheritance.add(m);
                continue parentTraverse;
            }
            break;
        }
    }
    
    public boolean isOverrideOf(Method m) {
        return inheritance.contains(m);
    }
    
    private void readAnnotations() {
        for(Method m:inheritance) {
            for(Annotation a:m.getDeclaredAnnotations()) {
                if (annotations.containsKey(a.annotationType()))
                    continue;
                annotations.put(a.annotationType(), a);
            }
        }
    }
    
    public boolean hasAnnotation(Class<? extends Annotation> aType) {
        return annotations.containsKey(aType);
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> aType) {
        return (T) annotations.get(aType);
    }
    
    private void readParameters() {
        String[] parmNames = paranamer.lookupParameterNames(method,false);
        Class<?>[] parmTypes = method.getParameterTypes();
        Type[] genParmTypes = method.getGenericParameterTypes();
        
        for(int i = 0;i < parmNames.length;i++) {
            if (genParmTypes.length <= i) {
                break;
            }

            Class type = parmTypes[i];
            
            Type genParmType = genParmTypes[i];
            if (genParmType instanceof TypeVariable) {
                genParmType = owner.getTypeVariableType((TypeVariable)genParmType);
                if (genParmType instanceof Class) {
                    type = (Class)genParmType;
                }
            }
            ClassInfo classInfo;
            if (genParmType instanceof ParameterizedType) {
                classInfo = ClassInfo.from(type,ClassInfo.readGenericTypes(genParmType, owner));
            } else {
                classInfo = ClassInfo.from(type,genParmType);
            }
            
            Annotation[] annos = new Annotation[0];

            for(Method m:inheritance) {
                annos = m.getParameterAnnotations()[i];
                if (annos.length > 0) break;
            }
            
            Parameter parm = new Parameter(parmNames[i],classInfo, annos);
            parameters.put(parm.getName(), parm);
        }
    }

    public Map<String, Parameter> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
    
    public Parameter getParameter(String name) {
        return parameters.get(name);
    }
    
    public Parameter getParameter(int offset) {
        int i = 0;
        for(Parameter p:parameters.values()) {
            if (i == offset)
                return p;
            i++;
        }
        return null;
    }
    
    public String getName() {
        return method.getName();
    }

    public ClassInfo getReturnType() {
        return returnType;
    }
    
    public Object invoke(Object instance,Object ... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return method.invoke(instance, args);
    }

    public boolean hasParmTypes(ClassInfo[] args) {
        if (parameters.size() != args.length)
            return false;
        ArrayList<Parameter> parms = new ArrayList<Parameter>(parameters.values());
        
        for(int i = 0; i < args.length;i++) {
            Parameter p = parms.get(i);
            if (!p.getType().isAssignableFrom(args[i])) {
                return false;
            }
        }
        return true;
    }
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }
    
    public boolean isPublic() {
        return Modifier.isPublic(method.getModifiers());
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MethodInfo other = (MethodInfo) obj;
        if (this.method != other.method && (this.method == null || !this.method.equals(other.method))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.method != null ? this.method.hashCode() : 0);
        return hash;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s::%s(",method.getDeclaringClass().getName(),getName()));
        boolean first = true;
        for(Parameter p:parameters.values()) {
            if (first)
                first = false;
            else
                sb.append(",");
            
            sb.append(p.toString());
        }
        sb.append(")");
        return sb.toString();
    }
    
    public String toSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s(",getName()));
        boolean first = true;
        for(Parameter p:parameters.values()) {
            if (first)
                first = false;
            else
                sb.append(",");
            
            sb.append(p.toString());
        }
        sb.append(")");
        return sb.toString();
    }
    
    
    
    public static class Parameter {
        private final String name;
        private final ClassInfo type;
        private final Map<Class<? extends Annotation>,Annotation> annotations = new HashMap<Class<? extends Annotation>, Annotation>();

        private Parameter(String name, ClassInfo type,Annotation[] annotations) {
            this.name = name;
            this.type = type;
            
            readAnnotations(annotations);
        }
        
        private void readAnnotations(Annotation[] annotations) {
            for(Annotation a:annotations) {
                this.annotations.put(a.annotationType(), a);
            }
        }

        public String getName() {
            return name;
        }

        public ClassInfo getType() {
            return type;
        }
        public boolean hasAnnotation(Class<? extends Annotation> aType) {
            return annotations.containsKey(aType);
        }
        public <T extends Annotation> T getAnnotation(Class<T> aType) {
            return (T) annotations.get(aType);
        }

        @Override
        public String toString() {
            return String.format("%s %s",type,name);
        }
        
        
    }

}
