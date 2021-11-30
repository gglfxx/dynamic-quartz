
package com.gvan.quartz.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Configuration
public class WebConfigurer implements WebMvcConfigurer {

    @Value("${spring.jackson.date-format}")
    private String dateFormatPattern;

    @Value("${spring.jackson.time-zone}")
    private String timeZone;

    @Value("${spring.jackson.locale}")
    private Locale locale;

    /**
     * jackon时间转换器
     * spring.jackson.date-format 这个属性在spring boot 1.x中有用，但是当你项目升级到spring boot2.x 以后，时间格式化变无效了
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = converter.getObjectMapper();
        // 生成JSON时,将所有Long转换成String
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        // 时间格式化
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //这个可以引用spring boot yml 里的格式化配置和时区配置
        objectMapper.setDateFormat(new SimpleDateFormat(dateFormatPattern));
        objectMapper.setTimeZone(TimeZone.getTimeZone(timeZone));
        objectMapper.setLocale(locale);
        // 设置格式化内容
        converter.setObjectMapper(objectMapper);
        converters.add(0, converter);
    }

}
