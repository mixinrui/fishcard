package com.boxfishedu.workorder.common.rabbitmq;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/4/14.
 */
@Data
@Component
public class RabbitMqConstant {
    public static final String EXCHANGE_NAME="boxfish.fishcard.exchange";
    public static final String UNASSIGNED_TEACHER_TEMPLATE_NAME="boxfish.fishcard.unsignedteacher.template";
    public static final String FINISH_FISHCARD_TEMPLATE_NAME="boxfish.fishcard.finishcard.teachingonline.template";
    public static final String ORDER_TO_SERVICE_TEMPLATE_NAME="com.boxfishedu.order.exchange.teaching.template";
    public static final String UNASSIGNED_TEACHER_TIMER_TEMPLATE_NAME="boxfish.fishcard.unsignedteacher.timer.template";
    public static final String ORDER_NOTIFY_TEMPALTE_NAME="boxfish.fishcard.notifyorder.template";
    public static final String UNASSIGNED_TEACHER_TIMER_REPLY_TEMPLATE_NAME="boxfish.fishcard.unsignedteacher.timer.reply.template";
    public static final String CREATE_GROUP_TEMPLATE_NAME = "com.boxfishedu.creategroup.template";
    public static final String ASSIGN_FOREIGN_TEACHER_COMMENT_TEMPLATE_NAME="boxfish.fishcard.workorder.commentcard.template";
    public static final String ALLOT_FOREIGN_TEACHER_COMMENT_TEMPLATE_NAME="boxfish.fishcard.student.teacher.commentcard.template";


    public static final String ASSIGN_TEACHER_TEMPLATE_NAME = "boxfish.assign.teacher.template";
    public static final String ASSIGN_TEACHER_REPLY_TEMPLATE_NAME = "boxfish.assign.teacher.reply.template";


    //与教学中心组的交互
    public static final String FISHCARD_CREATED_QUEUE ="boxfish.finishcard.created.queue";
    //向教师中心推送未分配教师的消息
    public static final String UNASSIGNED_TEACHER_QUEUE="boxfish.finishcard.unsigned.teacher.queue";
    //订单转换为服务和工单时候‰‰
    public static final String ORDER_TO_SERVICE_QUEUE="com.boxfishedu.order.service.queue";
    //教学中心发过来的消息处理
    public static final String UNASSIGNED_TEACHER_TIMER_QUEUE="boxfish.fishcard.unsignedteacher.timer.queue";
    //通知订单中心服务完成
    public static final String ORDER_NOTIFY_QUEUE="boxfish.fishcard.notifyorder.queue";
    //定时任务处理失败以后触发的机制
    public static final String UNASSIGNED_TEACHER_REPLY_TIMER_QUEUE="boxfish.fishcard.unsignedteacher.timer.reply.queue";

    //分配老师
    public static final String ASSIGNED_TEACHER_REQUEST_QUEUE = "boxfish.schedule.receive.queue.name.development";
    public static final String ASSIGNED_TEACHER_REPLY_QUEUE = "boxfish.schedule.send.queue.name.development";

    //请求分配外教点评老师
    public static final String ASSIGNED_FOREIGN_TEACHER_COMMENT_QUEUE = "boxfish.fishcard.comment.queue";
    //响应分配外教点评老师
    public static final String ALLOT_FOREIGN_TEACHER_COMMENT_QUEUE = "boxfish.teacher.student.comment.queue";
    //修改点评卡头像
    public static final String UPDATE_PICTURE_QUEUE = "update.picture.queue";

    //发送到小马的创建组的队列
    public static final String CREATE_GROUP_QUEUE = "com.boxfishedu.creategroup.queue";
    //教学中心返回的状态码,exchange和创建组使用一个exchange
    public static final String TEACHING_ONLINE_STATUS_QUEUE="com.boxfishedu.update_workorder_status";


    //延迟队列,用于教师旷课
    public static final String DELAY_QUEUE_TEACHER_ABSENT_TEMPLATE_NAME="boxfish.delay.absent.teacher.template";
    //延迟队列,无需接听
    public static final String DELAY_TEACHER_ABSENT_QUEUE="boxfish.delay.teacher.queue";
    //教师旷课处理
    public static final String DELAY_TEACHER_DEALER_QUEUE="boxfish.delay.teacher.dealer.queue";

    //延迟队列,用于学生旷课
    public static final String DELAY_QUEUE_STUDENT_ABSENT_TEMPLATE_NAME="boxfish.delay.absent.student.template";
    //延迟队列,无需接听
    public static final String DELAY_STUDENT_ABSENT_QUEUE="boxfish.delay.student.queue";
    //学生旷课处理
    public static final String DELAY_STUDENT_DEALER_QUEUE="boxfish.delay.student.dealer.queue";

    //延迟队列,用于强制下课
    public static final String DELAY_QUEUE_FORCE_COMPLETE_TEMPLATE_NAME="boxfish.delay.complete.force.template";
    //延迟队列,无需接听
    public static final String DELAY_COMPLETE_FORCE_QUEUE="boxfish.delay.complete.force.queue";
    //强制下课处理
    public static final String DELAY_COMPLETE_FORCE_DEALER_QUEUE="boxfish.delay.complete.force.dealer.queue";

    //延迟队列,用于通知教师准备上课
    public static final String DELAY_QUEUE_NOTIFY_TEACHER_PREPARE_TEMPLATE_NAME="boxfish.delay.notify.teacher.prepare.template";
    //延迟队列,无需接听
    public static final String DELAY__NOTIFY_TEACHER_PREPARE_QUEUE="boxfish.delay.notify.teacher.prepare.queue";
    //通知上课准备处理
    public static final String DELAY_NOTIFY_TEACHER_PREPARE_DEALER_QUEUE="boxfish.delay.notify.teacher.prepare.dealer.queue";
    //通知抢单
    public static final String GRAB_WORKER_ORDER_TIME_QUEUE= "boxfish.fishcard.graborder.timer.queue";

}
