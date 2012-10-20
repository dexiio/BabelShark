package com.vonhof.babelshark.annotation;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
@Inherited
public @interface TypeResolver{
    String field();
    String resolverMethod();
}
