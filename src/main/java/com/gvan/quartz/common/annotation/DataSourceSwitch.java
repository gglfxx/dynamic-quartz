package com.gvan.quartz.common.annotation;

import java.lang.annotation.*;

/**
 * 切换数据源注解
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSourceSwitch {
    String type();
}
