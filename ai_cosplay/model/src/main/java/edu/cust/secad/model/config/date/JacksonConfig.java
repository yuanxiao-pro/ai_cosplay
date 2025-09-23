package edu.cust.secad.model.config.date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * @ClassDescription:
 * @Author: Nvgu
 * @Created: 2024/11/15 18:46
 * @Updated: 2024/11/15 18:46
 */
public class JacksonConfig {
    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册 JavaTimeModule
        mapper.registerModule(new JavaTimeModule());
        // 启用序列化特性，防止日期/时间被转换为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}