package com.vonhof.babelshark;

import java.util.HashMap;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public class BeanMap<T> extends HashMap<String,Object> {
    private final T bean;
    public BeanMap(T bean) {
        this.bean = bean;
    }

    public T getBean() {
        return bean;
    }
    
}
