package com.vonhof.babelshark;

import com.vonhof.babelshark.exception.MappingException;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public interface BeanMapper {
    public <T> MappedBean<T> getMap(Class<T> type) throws MappingException;
}
