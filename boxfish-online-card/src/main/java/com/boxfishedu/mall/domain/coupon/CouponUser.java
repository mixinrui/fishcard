package com.boxfishedu.mall.domain.coupon;

import com.boxfishedu.mall.common.BaseEntity;
import com.boxfishedu.mall.enums.CouponUserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Data(staticConstructor = "getInstance")
@EqualsAndHashCode(callSuper = true)
public class CouponUser extends BaseEntity {

    private static final long serialVersionUID = -7807419385933873056L;

    private String createUser;

    private Long userId;

    private Long couponId;

    private String couponSn;

    @Enumerated(EnumType.STRING)
    private CouponUserStatus statusCode;

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date expiryDate;

    @Transient
    private CouponInfo couponInfo;

    public static CouponUser getInstance(CouponInfo coupon, Long userId) {
        CouponUser couponUser = CouponUser.getInstance();
        couponUser.setCouponInfo(coupon);
        couponUser.setCouponId(coupon.getId());
        couponUser.setUserId(userId);
        couponUser.setStatusCode(CouponUserStatus.UNUSED);
        couponUser.setCouponSn(UUID.randomUUID().toString());
        couponUser.setCreateUser(userId + "");
        couponUser.setCreateTime(new Date());
        //计算失效日期
        Date beginTime = coupon.getBeginTime();
        Integer validDays = coupon.getValidDays();
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.setTime(beginTime);
        expiryDate.add(Calendar.DAY_OF_MONTH, validDays);
        couponUser.setExpiryDate(expiryDate.getTime());

        return couponUser;
    }
}