package com.boxfishedu.mall.domain.coupon;

import com.boxfishedu.mall.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class CouponConditionValue extends BaseEntity {

    private static final long serialVersionUID = 582015748934316139L;

    private Long conditionId;

    private String conditionValue;
}
