package com.vonhof.babelshark.annotation;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.TYPE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface TypeResolver{
    String field();
    String resolverMethod();
}
