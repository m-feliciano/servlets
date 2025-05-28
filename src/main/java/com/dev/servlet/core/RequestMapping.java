package com.dev.servlet.core;

import com.dev.servlet.validator.Validator;
import com.dev.servlet.model.pojo.enums.RoleType;
import com.dev.servlet.model.pojo.enums.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value();

    RequestMethod method() default RequestMethod.GET;

    boolean requestAuth() default true;

    Validator[] validators() default {};

    String apiVersion() default "v1";

    RoleType[] roles() default {};
}
