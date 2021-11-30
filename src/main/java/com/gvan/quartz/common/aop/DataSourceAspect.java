package com.gvan.quartz.common.aop;

import com.gvan.quartz.common.annotation.DataSourceSwitch;
import com.gvan.quartz.common.datasource.DBContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * AOP动态切换数据源
 */
@Component
@Aspect
@Slf4j
public class DataSourceAspect {
    public DataSourceAspect(){
        System.out.println("this is init");
    }

    @Pointcut("@within(com.gvan.quartz.common.annotation.DataSourceSwitch) || " +
            "@annotation(com.gvan.quartz.common.annotation.DataSourceSwitch)")
    public void pointCut(){

    }

    @Before("pointCut() && @annotation(dataSourceSwitch)")
    public void doBefore(DataSourceSwitch dataSourceSwitch){
        log.debug("select dataSource---"+dataSourceSwitch.type());
        DBContextHolder.setDataSource(dataSourceSwitch.type());
    }

    @After("pointCut()")
    public void doAfter(){
        DBContextHolder.clearDataSource();
    }
}
