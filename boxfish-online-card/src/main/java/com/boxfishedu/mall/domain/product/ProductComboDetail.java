package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import lombok.Data;

@Data(staticConstructor = "getInstance")
public class ProductComboDetail extends BaseEntity {

    private static final long serialVersionUID = 261627605398655200L;

    public final static Integer DEFAULT_COMBO_CYCLE = 1;

    private Long comboId;

    private Long skuId;

    private ProductSku productSku;

    private Integer skuAmount;
}