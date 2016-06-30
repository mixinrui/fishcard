package com.boxfishedu.workorder.web.view.order;

import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.web.view.base.BaseTimeView;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLogView extends BaseTimeView {
    private OrderForm order;
    private Long userID;
    private String code;
    private BigDecimal totalPrice;
    private String status;
}
