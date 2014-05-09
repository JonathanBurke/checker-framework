package org.checkerframework.checker.tainting.qual;

import java.lang.annotation.*;

import org.checkerframework.checker.tainting.TaintingChecker;
import org.checkerframework.framework.qual.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface TaintingParams {
    TaintingParam[] value();
}

