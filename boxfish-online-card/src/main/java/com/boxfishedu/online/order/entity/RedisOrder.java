package com.boxfishedu.online.order.entity;

import com.boxfishedu.workorder.common.bean.OrderConvertEnum;
import lombok.Data;

import java.util.Date;

/**
 * Created by hucl on 16/5/18.
 */
@Data
public class RedisOrder {
    private OrderForm order;
    private int status;
    private long updateTime;

    public RedisOrder() {
    }

    public RedisOrder(OrderForm order) {
        this.order = order;
        this.status = OrderConvertEnum.WAIT_PROCESS.value();
        this.updateTime = new Date().getTime();
    }

    public RedisOrder(OrderForm order, OrderConvertEnum orderConvertEnum) {
        this.order = order;
        this.status = orderConvertEnum.value();
        this.updateTime = new Date().getTime();
    }
}
