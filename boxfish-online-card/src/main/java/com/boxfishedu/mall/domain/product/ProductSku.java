package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSku extends BaseEntity {

    private static final long serialVersionUID = 4775353166803465923L;

    private Long productId;

    private Integer skuPrice;

    // 国籍
    private String optionOne;

    // 人数
    private String optionTwo;

    private String optionThree;

    private String optionFour;

    private String optionFive;
}