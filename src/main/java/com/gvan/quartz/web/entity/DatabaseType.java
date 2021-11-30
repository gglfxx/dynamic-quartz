package com.gvan.quartz.web.entity;

/**
 * 数据库类型
 * @author guigl
 *
 */
public enum DatabaseType {

	ORACLE("oracle", "oracle.jdbc.OracleDriver"),
	SQLSERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
	MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
	DM("dm","dm.jdbc.driver.DmDriver");
	private final String key;
	private final String value;

	DatabaseType(final String key, final String value) {
		this.key = key;
		this.value = value;
	}
	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
