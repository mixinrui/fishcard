package com.boxfishedu.workorder.web.view.order;

import com.boxfishedu.workorder.web.view.base.BaseTimeView;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductView extends BaseTimeView {
    private String name;
    private Long code;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private String startTime;
    private String endTime;
    private Integer flagEnable;
    private Integer flagVisible;
}

