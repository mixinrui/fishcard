package com.boxfishedu.workorder.web.view.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class JsonResultModel implements Serializable {

    public static JsonResultModel newJsonResultModel(Object data) {
        return new JsonResultModel(data);
    }

    public static JsonResultModel newJsonResultModel() {
        return new JsonResultModel("ok");
    }

    private static ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)       // 属性为空（“”）或者为 NULL 都不序列化
            //        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)       // 这句要慎加,0会被当做empty忽略掉,有些type类型的字段就成了null
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            .setTimeZone(TimeZone.getTimeZone("GMT+8"));

    private Object data;

    private int returnCode = 200;

    private String returnMsg = "‰";

    private JsonResultModel(Object data) {
        this.data = data;
    }

    public JsonResultModel() {
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public Object getData() {
        return data;
    }

    /**
     * 解决类型转换的问题
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getData(Class<T> clazz) {
        if (data == null) {
            return null;
        }
        return objectMapper.convertValue(data, clazz);
    }

    public <T> List<T> getListData(Class<T> clazz) {
        ArrayList data = getData(ArrayList.class);
        if(data == null) {
            return null;
        }
        List<T> result = Lists.newArrayList();
        for(int i=0; i<data.size(); i++) {
            result.add(objectMapper.convertValue(data.get(i), clazz));
        }
        return result;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
}
