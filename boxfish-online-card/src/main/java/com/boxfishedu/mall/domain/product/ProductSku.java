package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductSku extends BaseEntity {

    private static final long serialVersionUID = 4775353166803465923L;

    private Long productId;

    private Integer skuPrice;

    private String optionOne;

    private String optionTwo;

    private String optionThree;

    private String optionFour;

    private String optionFive;
}