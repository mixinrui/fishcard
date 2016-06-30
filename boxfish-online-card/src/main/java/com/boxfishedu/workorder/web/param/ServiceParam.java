package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by hucl on 16/4/5.
 */
@Data
public class ServiceParam {
    private Long studentId;
    private String studentName;
    private Long orderId;
    private Integer originalAmount;
    private Integer amount;
    private String startTime;
    private String endTime;
    private Long skuId;
    private String skuName;
}
