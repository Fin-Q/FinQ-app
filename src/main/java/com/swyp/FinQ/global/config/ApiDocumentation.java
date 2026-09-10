package com.swyp.FinQ.global.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDocumentation {

    String id() default "";

    String name();

    ApiOwner owner() default ApiOwner.UNASSIGNED;

    boolean secured() default true;
}
