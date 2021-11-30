package com.gvan.quartz.common.datasource;

/**
 * 动态切换数据源
 */
public class DBContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    //调用此方法，切换数据源
    public static void setDataSource(String dataSource) {
        contextHolder.set(dataSource);
    }

    public static String getDataSource() {
        return contextHolder.get();
    }

    public static void clearDataSource() {
        contextHolder.remove();
    }
}
