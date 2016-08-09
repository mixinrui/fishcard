package com.boxfishedu.mall.domain.coupon;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.MatchMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class CouponCondition extends BaseEntity{

    private static final long serialVersionUID = 950206986126434951L;

    private Long couponId;

    private String conditionName;

    @Enumerated(EnumType.STRING)
    private MatchMode matchMode;

}