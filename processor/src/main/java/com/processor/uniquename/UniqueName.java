package com.processor.uniquename;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Labels that Class.getSimpleName() should be unique within the project
 *
 * @author dwang
 * @since 07.11.18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface UniqueName {
    String scope() default "default";
}
