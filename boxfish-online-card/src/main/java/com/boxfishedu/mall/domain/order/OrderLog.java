package com.boxfishedu.mall.domain.order;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class OrderLog extends BaseEntity {

    private static final long serialVersionUID = -6597529662575174442L;

    private Long orderId;

    private Long userId;

    private String orderCode;

    private Integer totalFee;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    public static OrderLog getInstance(OrderForm orderForm) {
        OrderLog instance = OrderLog.getInstance();
        instance.setOrderId(orderForm.getId());
        instance.setUserId(orderForm.getUserId());
        instance.setOrderCode(orderForm.getOrderCode());
        instance.setTotalFee(orderForm.getPayFee());
        instance.setOrderStatus(orderForm.getOrderStatus());
        instance.setCreateTime(new Date());
        return instance;
    }
}