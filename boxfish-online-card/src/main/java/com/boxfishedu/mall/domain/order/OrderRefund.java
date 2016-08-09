package com.boxfishedu.mall.domain.order;

import java.util.Date;

public class OrderRefund {
    private Long id;

    private Date createTime;

    private Date updateTime;

    private Long orderId;

    private String orderCode;

    private Integer refundFee;

    private Integer refundSn;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode == null ? null : orderCode.trim();
    }

    public Integer getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(Integer refundFee) {
        this.refundFee = refundFee;
    }

    public Integer getRefundSn() {
        return refundSn;
    }

    public void setRefundSn(Integer refundSn) {
        this.refundSn = refundSn;
    }
}