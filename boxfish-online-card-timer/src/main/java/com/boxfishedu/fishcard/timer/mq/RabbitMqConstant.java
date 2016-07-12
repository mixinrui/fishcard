package com.boxfishedu.fishcard.timer.mq;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/4/14.
 */
@Data
@Component
public class RabbitMqConstant {
    public static final String EXCHANGE_NAME="boxfish.fishcard.exchange";
    public static final String UNASSIGNED_TEACHER_TIMER_TEMPLATE_NAME="boxfish.fishcard.unsignedteacher.timer.template";
    public static final String UNASSIGNED_TEACHER_TIMER_REPLY_TEMPLATE_NAME="boxfish.fishcard.unsignedteacher.timer.reply.template";

    //订单转换为服务和工单时候
    public static final String UNASSIGNED_TEACHER_TIMER_QUEUE="boxfish.fishcard.unsignedteacher.timer.queue";
    public static final String UNASSIGNED_TEACHER_REPLY_TIMER_QUEUE="boxfish.fishcard.unsignedteacher.timer.reply.queue";

    //抢单队列
    public static final String GRAB_WORKER_ORDER_TIME_QUEUE= "boxfish.fishcard.graborder.timer.queue";
}
