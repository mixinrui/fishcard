package com.boxfishedu.mall.domain.order;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.Flag;
import com.boxfishedu.mall.enums.OrderChannel;
import com.boxfishedu.mall.enums.OrderSource;
import com.boxfishedu.mall.enums.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.List;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class OrderForm extends BaseEntity {

    private static final long serialVersionUID = 4677220636490502012L;

    private String orderCode;

    private Long userId;

    private Integer orderFee;

    private Integer couponFee;

    private Integer payFee;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private OrderSource orderSource;

    @Enumerated(EnumType.STRING)
    private OrderChannel orderChannel;

    private String payChannel;

    private Integer totalAmount;

    private String couponCode;

    private String orderRemark;

    private String description;

    @Enumerated(EnumType.STRING)
    private Flag flagDropped;

    @Transient
    private List<OrderDetail> orderDetails;

    @Transient
    private List<OrderLog> orderLogs;

}