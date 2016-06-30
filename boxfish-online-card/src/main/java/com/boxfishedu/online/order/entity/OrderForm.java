package com.boxfishedu.online.order.entity;

import com.boxfishedu.workorder.web.view.base.BaseTimeView;
import com.boxfishedu.workorder.web.view.order.OrderDetailView;
import com.boxfishedu.workorder.web.view.order.OrderLogView;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class OrderForm extends BaseTimeView {
    private Long userId;
    private String userName;
    private String orderCode;
    private String payFee;
    private String orderFee;
    private String couponFee;
    private String remark;
    private String orderSource;
    private BigDecimal totalPrice;
    private String payTime;
    private String orderStatus;
    private String remarkTitle;
    private String remarkDesc;
    private String payChannel;
    private Object orderChannel;
    private String inviteCode;
    private String flagDropped;
    private Set<OrderLogView> orderLogs;
    private Set<OrderDetailView> orderDetails;
}
