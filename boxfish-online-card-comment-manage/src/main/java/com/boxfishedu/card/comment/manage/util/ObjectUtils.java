package com.boxfishedu.card.comment.manage.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by LuoLiBing on 16/5/12.
 */
public class ObjectUtils {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static  <T> T convertObject(Object source, Class<T> clazz) {
        return objectMapper.convertValue(source, clazz);
    }

    public static String convertToString(Object src) throws JsonProcessingException {
        return objectMapper.writeValueAsString(src);
    }


    public static void beanCopyProperties(Object src, Object target) {
        try {
            BeanUtils.copyProperties(src, target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
