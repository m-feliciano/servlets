package com.dev.servlet.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validator {

    String[] values();

    Constraints[] constraints() default @Constraints();

    Class<? extends Throwable> exception() default RuntimeException.class;
}