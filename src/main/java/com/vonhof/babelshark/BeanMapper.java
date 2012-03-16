package com.vonhof.babelshark;

import com.vonhof.babelshark.exception.MappingException;

/**
 *
 * @author Henrik Hofmeister <hh@cphse.com>
 */
public interface BeanMapper {
    public <T> MappedBean<T> getMap(Class<T> type) throws MappingException;
}
