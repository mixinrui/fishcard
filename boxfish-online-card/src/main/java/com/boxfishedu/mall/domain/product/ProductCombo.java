package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.Flag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.List;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class ProductCombo extends BaseEntity {

    private static final long serialVersionUID = 884403589206290959L;

    private String comboType;

    private Integer originalFee;

    private Integer couponFee;

    private Integer payFee;

    private Integer totalAmount;

    private String comboDesc;

    @Enumerated(EnumType.STRING)
    private Flag flagEnable = Flag.ENABLE;

    @Transient
    private List<ProductComboDetail> comboDetails;
}