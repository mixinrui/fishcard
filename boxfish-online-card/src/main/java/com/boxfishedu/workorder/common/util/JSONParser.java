package com.boxfishedu.workorder.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by liuzhihao on 16/3/16.
 */
public class JSONParser {
    private static Logger logger = LoggerFactory.getLogger(JSONParser.class);

    static ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)       // 属性为空（“”）或者为 NULL 都不序列化
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.EAGER_SERIALIZER_FETCH)
            .setTimeZone(TimeZone.getTimeZone("GMT+8"));

    static public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("[toJson] 功能异常: {}", e.toString());
            return null;
        }
    }

    static public <T> T fromJson(String json, Class<T> type) {
        try {
            if (StringUtils.isEmpty(json)) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            logger.error("[toJson] 功能异常: {}", e.toString());
            e.printStackTrace();
        }
        return null;
    }

    static public String getProperty(String json, String propName) {
        try {
            if (StringUtils.isEmpty(json)) {
                return "";
            }
            HashMap properties = objectMapper.readValue(json, HashMap.class);
            Object o = properties.get(propName);
            if (o == null) {
                return "";
            }
            return o.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}