package com.boxfishedu.workorder.web.param;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by jiaozijun on 17/2/6.
 */

@Data
public class ExcelPageAble implements Serializable {


    private int page;
    private int size;
}
