package com.boxfishedu.mall.domain.coupon;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.CouponRule;
import com.boxfishedu.mall.enums.CouponStatus;
import com.boxfishedu.mall.enums.Flag;
import com.boxfishedu.mall.enums.GiftMode;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class CouponInfo extends BaseEntity {

    private static final long serialVersionUID = 8817679856881791817L;

    private String createUser;

    private String updateUser;

    private Long activityId;

    private String couponCode;

    @Enumerated(EnumType.STRING)
    @JsonView(SimpleEntity.class)
    private CouponRule couponRule;

    @JsonView(SimpleEntity.class)
    private Integer couponDiscount;

    @JsonView(SimpleEntity.class)
    private Long skuId;

    @JsonView(SimpleEntity.class)
    private Long comboId;

    @Enumerated(EnumType.STRING)
    private GiftMode giftMode;

    private Integer giftAmount;

    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus;

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer validDays;

    @Enumerated(EnumType.STRING)
    private Flag flagEnable = Flag.ENABLE;
}