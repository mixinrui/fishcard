package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.Flag;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.List;

@Data(staticConstructor = "getInstance")
public class ProductOption extends BaseEntity {

    private static final long serialVersionUID = 4586015255652379024L;

    private Long productId;

    private String optionCode;

    private String optionName;

    private String refSkuColumn;

    private String optionRemark;

    @Enumerated(EnumType.STRING)
    private Flag flagEnable = Flag.ENABLE;

    @Transient
    private List<ProductOptionValue> productOptionValues;
}