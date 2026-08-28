package com.github.sunhaipeng.businesstrace.sdk.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceMethod {
    String operationName() default "";
    boolean captureParams() default true;
    boolean captureResult() default true;
}