package com.gvan.quartz.web.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据源实体类
 */
@Data
public class TargetDataSource implements Serializable {
	private static final long serialVersionUID = 8635768544065924301L;
    //任务id
    private int id;
    //系统代码
    private String sysNo;
    //系统id
    private String sysId;
    //数据库地址
    private String dbUrl;
    //唯一标识名称
    private String dbKey;
    //驱动
    private String driveClass;
    //数据库用户名
    private String username;
    //数据库密码
    private String password;
    //数据库类型
    private String databaseType;
}
