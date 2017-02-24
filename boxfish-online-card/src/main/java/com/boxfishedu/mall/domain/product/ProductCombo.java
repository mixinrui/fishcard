package com.boxfishedu.mall.domain.product;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.mall.enums.Flag;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.List;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductCombo extends BaseEntity {

    private static final long serialVersionUID = 884403589206290959L;

    @Enumerated(EnumType.STRING)
    private ComboTypeToRoleId comboType;

    private String comboCode;

    private Integer originalFee;

    private Integer couponFee;

    private Integer payFee;

    private Integer totalAmount;

    private String comboDesc;

    private String comboUnit;

    // 班级人数
    private Integer classSize;

    //小班课id
    private Long smallClassId;

    private Integer comboCycle;//限制该套餐在几周内完成,不需要限制的标识为-1,用于需要选时间的订单在生成服务时计算选时间的次数

    @Enumerated(EnumType.STRING)
    private Flag flagEnable;

    @Transient
    private List<ProductComboDetail> comboDetails;

    @Transient
    private List<ComboDurations> comboDurations;
}