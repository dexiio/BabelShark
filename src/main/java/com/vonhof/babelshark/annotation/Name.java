
package com.vonhof.babelshark.annotation;

import java.lang.annotation.*;

/**
 * Use this name in mapping instead of the actual name
 * @author Henrik Hofmeister <henrik@newdawn.dk>
 */

@Documented
@Target(value={ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Name {
    /**
     * Name of the field
     * @return 
     */
    String value() default "";
    /**
     * Optional description - for use in auto documentation
     * @return 
     */
    String description() default "";
    
    /**
     * Will cause an exception if field is not present
     * @return 
     */
    boolean required() default false;
    
    /**
     * Indicate that if available - the field / property should be (de)serialized as an attribute
     * Usable for XML and similar. JSON and other formats can simply ignore this value
     * @return 
     */
    boolean attribute() default false;
}
