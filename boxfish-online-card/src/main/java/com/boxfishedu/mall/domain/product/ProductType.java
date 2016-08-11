package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.Flag;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data(staticConstructor = "getInstance")
public class ProductType extends BaseEntity {

    private static final long serialVersionUID = 5081845663088105186L;

    private String typeCode;

    private String typeName;

    @Enumerated(value = EnumType.STRING)
    private Flag flagEnable = Flag.ENABLE;
}