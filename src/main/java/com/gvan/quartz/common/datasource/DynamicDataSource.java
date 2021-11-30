package com.gvan.quartz.common.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidDataSourceStatManager;
import com.gvan.quartz.web.entity.DatabaseType;
import com.gvan.quartz.web.entity.TargetDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Set;

/**
 * 动态配置多数据源
 */
@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    private boolean debug = false;

    private Map<Object, Object> dynamicTargetDataSources;

    private Object dynamicDefaultTargetDataSource;

    /* (non-Javadoc)
        @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource#determineCurrentLookupKey()
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String datasource=DBContextHolder.getDataSource();
        if(debug)
        {
            if(StringUtils.isEmpty(datasource)){
                log.info("---当前数据源：默认数据源---");
            }else{
                log.info("---当前数据源："+datasource+"---");
            }
        }

        return datasource;
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        this.dynamicTargetDataSources = targetDataSources;
    }
    //创建数据源
    public boolean createDataSource(TargetDataSource targetDataSource){
        try {
            try {
                //如果已经注册无需重复注册
                if(this.dynamicTargetDataSources.containsKey(targetDataSource.getDbKey())){
                    return true;
                }
                //排除连接不上的错误
                Class.forName(targetDataSource.getDriveClass());
                DriverManager.getConnection(targetDataSource.getDbUrl(), targetDataSource.getUsername(), targetDataSource.getPassword());
            } catch (Exception e) {
                log.warn("检查数据库连接异常："+e.getMessage());
                return false;
            }
            @SuppressWarnings("resource")
			DruidDataSource druidDataSource = new DruidDataSource();
            druidDataSource.setName(targetDataSource.getDbKey());
            druidDataSource.setDriverClassName(targetDataSource.getDriveClass());
            druidDataSource.setUrl(targetDataSource.getDbUrl());
            druidDataSource.setUsername(targetDataSource.getUsername());
            druidDataSource.setPassword(targetDataSource.getPassword());
            druidDataSource.setMinIdle(5);
            druidDataSource.setInitialSize(5);
            druidDataSource.setMaxActive(20);
            druidDataSource.setMaxWait(60000);
            druidDataSource.setFilters("stat");
            DataSource createDataSource = (DataSource)druidDataSource;
            String validationQuery = "select 1 from dual";
            if(DatabaseType.MYSQL.getKey().equalsIgnoreCase(targetDataSource.getDatabaseType())||
                    DatabaseType.SQLSERVER.getKey().equalsIgnoreCase(targetDataSource.getDatabaseType())) {
                validationQuery = "select 1";
            } else if(DatabaseType.ORACLE.getKey().equalsIgnoreCase(targetDataSource.getDatabaseType())){
                //是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭。
                druidDataSource.setPoolPreparedStatements(true);
                druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(50);
                //int sqlQueryTimeout = ADIPropUtil.sqlQueryTimeOut();
                //druidDataSource.setConnectionProperties("oracle.net.CONNECT_TIMEOUT=6000;oracle.jdbc.ReadTimeout="+sqlQueryTimeout);//对于耗时长的查询sql，会受限于ReadTimeout的控制，单位毫秒
            }
            //申请连接时执行validationQuery检测连接是否有效，这里建议配置为TRUE，防止取到的连接不可用
            druidDataSource.setTestOnBorrow(true);
            //建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
            druidDataSource.setTestWhileIdle(true);
            //用来检测连接是否有效的sql，要求是一个查询语句。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用。
            druidDataSource.setValidationQuery(validationQuery);
            //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
            druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
            //配置一个连接在池中最小生存的时间，单位是毫秒，这里配置为3分钟180000
            druidDataSource.setMinEvictableIdleTimeMillis(180000);
            //打开druid.keepAlive之后，当连接池空闲时，池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作，
            druidDataSource.setKeepAlive(true);
            // 即执行druid.validationQuery指定的查询SQL，一般为select * from dual，只要minEvictableIdleTimeMillis设置的小于防火墙切断连接时间，就可以保证当连接空闲时自动做保活检测，不会被防火墙切断
            //是否移除泄露的连接/超过时间限制是否回收。
            druidDataSource.setRemoveAbandoned(true);
            //泄露连接的定义时间(要超过最大事务的处理时间)；单位为秒。这里配置为1小时
            druidDataSource.setRemoveAbandonedTimeout(3600);
            //移除泄露连接发生是是否记录日志
            druidDataSource.setLogAbandoned(true);
            druidDataSource.init();
            Map<Object, Object> dynamicTargetDataSources2 =  this.dynamicTargetDataSources;
            dynamicTargetDataSources2.put(targetDataSource.getDbKey(), createDataSource);//加入map
            setTargetDataSources(dynamicTargetDataSources2);//将map赋值给父类的TargetDataSources
            super.afterPropertiesSet();//将TargetDataSources中的连接信息放入resolvedDataSources管理
            return true;
        } catch (Exception e) {
            log.error("动态注册数据库连接异常："+e.getMessage());
            return false;
        }
    }
    //删除数据源
    public boolean delDataSources(String dataSourceId){
        Map<Object, Object> dynamicTargetDataSources2 =  this.dynamicTargetDataSources;
        if(dynamicTargetDataSources2.containsKey(dataSourceId)){
            Set<DruidDataSource> druidDataSourceInstances = DruidDataSourceStatManager.getDruidDataSourceInstances();
            for(DruidDataSource dataSource:druidDataSourceInstances){
                if(dataSourceId.equals(dataSource.getName())){
                    dynamicTargetDataSources2.remove(dataSourceId);
                    DruidDataSourceStatManager.removeDataSource(dataSource);
                    setTargetDataSources(dynamicTargetDataSources2);//将map赋值给父类的TargetDataSources
                    super.afterPropertiesSet();//将TargetDataSources中的连接信息放入resolvedDataSources管理
                    return true;
                }
            }
            return false;
        }else{
            return false;
        }
    }

    //测试数据源连接是否有效
    public boolean testDatasource(String key,String driveClass,String url,String username,String password){
        try {
            Class.forName(driveClass);
            DriverManager.getConnection(url, username, password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * Specify the default target DataSource, if any.
     * <p>The mapped value can either be a corresponding {@link DataSource}
     * instance or a data source name String (to be resolved via a
     * {@link #setDataSourceLookup DataSourceLookup}).
     * <p>This DataSource will be used as target if none of the keyed
     * {@link #setTargetDataSources targetDataSources} match the
     * {@link #determineCurrentLookupKey()} current lookup key.
     */
    public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        this.dynamicDefaultTargetDataSource = defaultTargetDataSource;
    }
    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @return the dynamicTargetDataSources
     */
    public Map<Object, Object> getDynamicTargetDataSources() {
        return dynamicTargetDataSources;
    }

    /**
     * @param dynamicTargetDataSources the dynamicTargetDataSources to set
     */
    public void setDynamicTargetDataSources(
            Map<Object, Object> dynamicTargetDataSources) {
        this.dynamicTargetDataSources = dynamicTargetDataSources;
    }

    /**
     * @return the dynamicDefaultTargetDataSource
     */
    public Object getDynamicDefaultTargetDataSource() {
        return dynamicDefaultTargetDataSource;
    }

    /**
     * @param dynamicDefaultTargetDataSource the dynamicDefaultTargetDataSource to set
     */
    public void setDynamicDefaultTargetDataSource(
            Object dynamicDefaultTargetDataSource) {
        this.dynamicDefaultTargetDataSource = dynamicDefaultTargetDataSource;
    }

}
