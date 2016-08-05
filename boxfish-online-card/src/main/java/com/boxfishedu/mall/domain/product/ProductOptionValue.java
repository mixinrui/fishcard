package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.Flag;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data(staticConstructor = "getInstance")
public class ProductOptionValue extends BaseEntity {

    private static final long serialVersionUID = -2895103405614976619L;

    private Long optionId;

    private String optionValue;

    private String valueRemark;

    @Enumerated(EnumType.STRING)
    private Flag flagEnable = Flag.ENABLE;
}