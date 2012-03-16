package com.vonhof.babelshark.annotation;

import java.lang.annotation.*;

/**
 * Allows you to provide a method that takes a single argument of ObjectNode - and lets you return a new instance
 * of the class.
 * @author Henrik Hofmeister <hh@cphse.com>
 */
@Documented
@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface FactoryMethod {
    String value();
}
