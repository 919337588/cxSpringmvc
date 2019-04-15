package com.chixin.note;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 自定义注解 注入到Spring容器
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CxControl {

}
