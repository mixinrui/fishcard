package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import lombok.Data;

@Data(staticConstructor = "getInstance")
public class ProductComboDetail extends BaseEntity {

    private static final long serialVersionUID = 261627605398655200L;

    private Long comboId;

    private Long skuId;

    private Integer skuAmount;
}