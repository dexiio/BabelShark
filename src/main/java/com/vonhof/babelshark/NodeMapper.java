/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vonhof.babelshark;

import com.vonhof.babelshark.exception.MappingException;
import com.vonhof.babelshark.node.SharkNode;
import com.vonhof.babelshark.node.SharkType;
import com.vonhof.babelshark.reflect.ClassInfo;

/**
 *
 * @author Henrik Hofmeister <@vonhofdk>
 */
public interface NodeMapper {
    public <T> T readAs(SharkNode obj, Class<T> type) throws MappingException;
    public <T> T readAs(SharkNode obj, ClassInfo<T> type) throws MappingException;
    public <T,U> T readAs(SharkNode node, SharkType<T,U> type) throws MappingException;
    public SharkNode toNode(Object obj) throws MappingException;
}
