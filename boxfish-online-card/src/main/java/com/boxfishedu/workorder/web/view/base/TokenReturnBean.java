package com.boxfishedu.workorder.web.view.base;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * Created by jiaozijun on 16/10/19.
 */


@Data
public class TokenReturnBean {

    private Integer code;

    private String message;

    private JSONObject data;
}