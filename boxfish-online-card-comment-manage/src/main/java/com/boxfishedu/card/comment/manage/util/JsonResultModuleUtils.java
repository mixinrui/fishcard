package com.boxfishedu.card.comment.manage.util;

import com.boxfishedu.beans.view.JsonResultModel;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/5/11.
 */
public class JsonResultModuleUtils {

    // private final static ObjectMapper objectMapper = new ObjectMapper();

    private final static List EMPTY = Lists.newArrayList();

    public static <T> List<T> getListFormResult(JsonResultModel jsonResultModel, Class<T> clazz, Handle<T> handle) {
        List resultList = jsonResultModel.getData(List.class);
        if(resultList == null) {
            return EMPTY;
        }
        List<T> result = Lists.newArrayList();
        resultList.forEach(beanMap -> {
            //result.add(objectMapper.convertValue(beanMap, clazz));
            result.add(handle.handle(clazz, (Map) beanMap));
        });
        return result;
    }

    public static <T> List<T> getListFromPageResult(JsonResultModel jsonResultModel, Class<T> clazz, Handle<T> handle) {
        HashMap hashMap = jsonResultModel.getData(HashMap.class);
        if(hashMap == null) {
            return EMPTY;
            //throw new RuntimeException("返回的结果为空");
        }
        Object content = hashMap.get("content");
        if(content == null) {
            return EMPTY;
        }

        if(!(content instanceof List)) {
            throw new RuntimeException("content 不为list");
        }
        return (List<T>) ((List)content)
                .stream()
                .map(beanMap -> handle.handle(clazz, (Map) beanMap))
                .collect(Collectors.toList());
    }

    public static Integer getTotalElements(JsonResultModel jsonResultModel) {
        HashMap hashMap = jsonResultModel.getData(HashMap.class);
        if(hashMap == null) {
            return 0;
            //throw new RuntimeException("返回的结果为空");
        }
        return hashMap.get("totalElements") == null ? 0 : (Integer) hashMap.get("totalElements");
    }

    @FunctionalInterface
    public interface Handle<T> {
        T handle(Class<T> clazz, Map beanMap);
    }
}
