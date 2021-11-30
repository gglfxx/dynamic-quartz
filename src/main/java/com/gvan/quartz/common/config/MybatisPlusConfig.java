package com.gvan.quartz.common.config;


import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.incrementer.H2KeyGenerator;
import com.baomidou.mybatisplus.extension.incrementer.OracleKeyGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.gvan.quartz.common.datasource.DynamicDataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * mybatis-plus 配置分页
 * 自定义数据源一定要排除SpringBoot自动配置数据源，不然会出现循环引用的问题
 *  @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
 */
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties(MybatisPlusProperties.class)
@MapperScan(basePackages = "com.gvan.quartz.web.dao", sqlSessionFactoryRef = "sqlSessionFactory")
public class MybatisPlusConfig {

	private MybatisPlusProperties mybatisPlusProperties;

	public MybatisPlusConfig(MybatisPlusProperties properties) {
		this.mybatisPlusProperties = properties;
	}

	/**
	 * 3.4.0 mybatis得分页拦截器以及乐观锁配置在 MybatisPlusInterceptor中
	 * @return
	 */
	@Bean("mybatisPlusInterceptor")
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.ORACLE_12C));
		interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
		return interceptor;
	}

	@Bean
	public GlobalConfig globalConfig() {
		GlobalConfig conf = new GlobalConfig();
		conf.setDbConfig(new GlobalConfig.DbConfig().setKeyGenerator(new H2KeyGenerator()));
		return conf;
	}

	@Bean(name = "master-db")
	@ConfigurationProperties(prefix = "spring.datasource.druid.master")
	public DataSource db1() {
		return DruidDataSourceBuilder.create().build();
	}

	@Bean(name = "slave-db")
	@ConfigurationProperties(prefix = "spring.datasource.druid.slave")
	public DataSource db2() {
		return DruidDataSourceBuilder.create().build();
	}

	/**
	 * 动态数据源配置
	 *
	 * @return DataSource
	 */
	@Bean("dynamicDataSource")
	@Qualifier("dynamicDataSource")
	@Primary
	public DynamicDataSource multipleDataSource(@Qualifier("master-db") DataSource master,
												@Qualifier("slave-db") DataSource slave) {
		DynamicDataSource dynamicDataSource = new DynamicDataSource();
		Map<Object, Object> targetDataSources = new HashMap<>();
		targetDataSources.put("master-db", master);
		targetDataSources.put("slave-db", slave);
		dynamicDataSource.setTargetDataSources(targetDataSources);
		// 程序默认数据源，这个要根据程序调用数据源频次，经常把常调用的数据源作为默认
		dynamicDataSource.setDefaultTargetDataSource(master);
		return dynamicDataSource;
	}

	@Bean("sqlSessionFactory")
	@Primary
	public SqlSessionFactory sqlSessionFactory(@Qualifier("dynamicDataSource") DataSource dataSource) throws Exception {
		MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setConfiguration(mybatisPlusProperties.getConfiguration());
		factoryBean.setMapperLocations(mybatisPlusProperties.resolveMapperLocations());
		factoryBean.setTypeAliasesPackage(mybatisPlusProperties.getTypeAliasesPackage());
		factoryBean.setPlugins(new Interceptor[]{
				mybatisPlusInterceptor() //添加分页功能
		});
		return factoryBean.getObject();
	}

	/**
	 * 配置事务管理器
	 */
	@Bean
	public PlatformTransactionManager transactionManager(DynamicDataSource dataSource){
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	public OracleKeyGenerator oracleKeyGenerator(){
		return new OracleKeyGenerator();
	}
}
