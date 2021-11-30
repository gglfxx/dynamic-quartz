package com.gvan.quartz.web.service.impl;

import com.gvan.quartz.job.QuartzFactory;
import com.gvan.quartz.web.entity.JobOperateEnum;
import com.gvan.quartz.web.entity.SysTaskSchedule;
import com.gvan.quartz.web.service.IQuartzService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Map;

/**
 * 配置定时任务实现类
 */
@Service
@Slf4j
public class QuartzServiceImpl implements IQuartzService {

    //调度器
    private Scheduler scheduler;
    @Autowired
    public void SetScheduler (Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 动态添加定时任务
     */
    @Override
    public void addJob(SysTaskSchedule job, Map<String, Object> param) {
        try {
            JobKey jobKey = null;
            //判断任务是否存在
            if(!checkJob(job)){
                //程序初始化时判断任务是否存在并当前状态为启用
                if("1".equals(job.getSchedule())) {
                    //创建触发器
                    Trigger trigger = TriggerBuilder.newTrigger().withIdentity(job.getTaskNo())
                            .withSchedule(CronScheduleBuilder.cronSchedule(job.getTaskExpress()))
                            .startNow()
                            .build();

                    //创建任务
                    JobDetail jobDetail = JobBuilder.newJob(QuartzFactory.class)
                            .withIdentity(job.getTaskNo(), job.getTaskGroup())
                            .build();

                    //传入调度的数据，在QuartzFactory中需要使用
                    jobDetail.getJobDataMap().put("scheduleJob", job);
                    //参数
                    jobDetail.getJobDataMap().put("param", param);

                    //调度作业
                    scheduler.scheduleJob(jobDetail, trigger);
                }
            }else{
                /**
                 * 首先判断当前任务是否启动　再判断前台页面触发任务的状态　
                 * 如果当前任务为启动，页面触发任务为２暂停，则暂停任务
                 * 如果当前任务为暂停，页面触发任务为１启动，则启动任务
                 * 任务启动状态NORMAL为正常　PAUSED为暂停
                 * 暂时不需要处理修改定时任务的时间规则
                 */
                TriggerKey triggerKey = TriggerKey.triggerKey(job.getTaskNo());
                Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
                //任务状态
                String name = triggerState.name();
                modifyJobCron(job.getTaskNo(),null,job.getTaskExpress(),name);
                jobKey = new JobKey(job.getTaskNo(),job.getTaskGroup());
                if("PAUSED".equals(name)&&"1".equals(job.getSchedule())){
                    scheduler.resumeJob(jobKey);
                }else if("NORMAL".equals(name)&&"2".equals(job.getSchedule())){
                    scheduler.pauseJob(jobKey);
                }
            }
        } catch (Exception e) {
            log.error("动态添加任务异常:",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 是否启动该任务
     * @param job
     * @return
     */
    private boolean checkJob(SysTaskSchedule job) {
        boolean success = true;
        JobKey jobKey = null;
        try{
            jobKey = new JobKey(job.getTaskNo(),job.getTaskGroup());
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (null== jobDetail) {
                success = false;
            }
        }catch (Exception e){
            success = false;
            log.error("检查任务状态异常：",e.getMessage());
        }
        return success;
    }

    /**
     * 修改job时间
     * @param name　任务名称
     * @param group 任务组
     * @param time 新的ｃron表达式
     * @param taskStatus 任务状态
     * @return
     * @throws SchedulerException
     */
    private boolean modifyJobCron(String name, String group, String time,String taskStatus) throws SchedulerException {
        Date date = null;
        TriggerKey triggerKey = new TriggerKey(name, group);
        CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        String oldTime = cronTrigger.getCronExpression();
        log.info("当前任务号为{},任务状态为{},cron旧表达式为{},新的表达式为{}",name,taskStatus,oldTime,time);
        if (!oldTime.equalsIgnoreCase(time)) {
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(time);
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
                    .withSchedule(cronScheduleBuilder).build();
            date = scheduler.rescheduleJob(triggerKey, trigger);
        }
        return date != null;
    }

    /**
     * 操作定时任务
     * @param jobOperateEnum 操作枚举
     * @param job 任务
     * @throws SchedulerException
     */
    @Override
    public void operateJob(JobOperateEnum jobOperateEnum, SysTaskSchedule job) throws SchedulerException {

    }

    /**
     * 启动所有定时任务
     * @throws SchedulerException
     */
    @Override
    public void startAllJob() throws SchedulerException {

    }

    /**
     * 暂停所有任务
     * @throws SchedulerException
     */
    @Override
    public void pauseAllJob() throws SchedulerException {

    }
}
