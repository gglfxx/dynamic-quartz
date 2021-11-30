package com.gvan.quartz.job;

import com.gvan.quartz.common.util.SpringUtil;
import com.gvan.quartz.web.entity.SysTaskSchedule;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 定时任务执行
 */
@Slf4j
//串行注解
@DisallowConcurrentExecution
public class QuartzFactory implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        //获取调度数据
        SysTaskSchedule scheduleJob = (SysTaskSchedule) jobExecutionContext.getMergedJobDataMap().get("scheduleJob");
        //传参
        Map<String,Object> param = (Map<String, Object>) jobExecutionContext.getMergedJobDataMap().get("param");
        //获取对应的Bean
        Object object = SpringUtil.getBean(scheduleJob.getTaskClass());
        try {
            //利用反射执行对应方法
            Method method = object.getClass().getMethod(scheduleJob.getTaskMethod(),
                    Map.class);
            method.invoke(object,param);
        } catch (Exception e) {
            log.error("定时任务执行出错：",e.getMessage());
        }
    }
}
