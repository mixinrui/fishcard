package com.boxfishedu.workorder.web.view.order;

import com.boxfishedu.workorder.web.view.base.BaseTimeView;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDetailView extends BaseTimeView {
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private Integer amount;
    private String description;
    private String productInfo;
    private String comboCycle;
    private String countInMonth;
}
