package com.boxfishedu.mall.domain.order;

import com.boxfishedu.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class OrderDetail extends BaseEntity {

    private static final long serialVersionUID = 1283729466466797203L;

    private Long orderId;

    private Long skuId;

    private Long skuPrice;

    private Integer skuAmount;

    private String productInfo;
}