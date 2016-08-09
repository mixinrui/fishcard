package com.boxfishedu.mall.domain.coupon;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.Flag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class CouponActivity extends BaseEntity {

    private static final long serialVersionUID = 2274275857686726345L;

    private String createUser;

    private String updateUser;

    private Date startTime;

    private Date endTime;

    @Enumerated(EnumType.STRING)
    private Flag flagExclude;

    private String activityCode;

    private String activityName;

    private String activityDesc;

    @Enumerated(EnumType.STRING)
    private Flag flagEnable = Flag.ENABLE;
}