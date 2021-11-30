package com.gvan.quartz.web.service;

import com.gvan.quartz.web.entity.JobOperateEnum;
import com.gvan.quartz.web.entity.SysTaskSchedule;
import org.quartz.SchedulerException;
import java.util.Map;

/**
 * 配置定时任务
 */
public interface IQuartzService {
    /**
     * 新增定时任务
     * @param job 任务
     */
    void addJob(SysTaskSchedule job, Map<String,Object> param);

    /**
     * 操作定时任务
     * @param jobOperateEnum 操作枚举
     * @param job 任务
     */
    void operateJob(JobOperateEnum jobOperateEnum, SysTaskSchedule job) throws SchedulerException;

    /**
     * 启动所有任务
     */
    void startAllJob() throws SchedulerException;

    /**
     * 暂停所有任务
     */
    void pauseAllJob() throws SchedulerException;

}
