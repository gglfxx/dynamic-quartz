package com.gvan.quartz.web.entity;

import lombok.Data;

@Data
public class SysTaskSchedule {
	private static final long serialVersionUID = 8635768544065924301L;
    private String taskNo;

    private String taskName;

    private String taskGroup;

    private String taskClass;

    private String taskMethod;

    private String taskExpress;
    
    private String schedule;
}
