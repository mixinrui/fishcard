package com.boxfishedu.workorder.common.rabbitmq;

import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
public class RabbitMqSender {
    private Logger logger = LoggerFactory.getLogger(getClass());

//    @Autowired
//    private @Qualifier(RabbitMqConstant.UNASSIGNED_TEACHER_TEMPLATE_NAME)RabbitTemplate unsignedTeacherRabbitTemplate;

//    @Autowired
//    private @Qualifier(RabbitMqConstant.FINISH_FISHCARD_TEMPLATE_NAME)RabbitTemplate teachingOnlineRabbitTemplate;

    @Autowired
    private @Qualifier(RabbitMqConstant.ORDER_NOTIFY_TEMPALTE_NAME) RabbitTemplate notifyOrderTemplate;
    @Autowired
    private @Qualifier(RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_REPLY_TEMPLATE_NAME) RabbitTemplate notifyTimerTemplate;
    @Autowired
    private @Qualifier(RabbitMqConstant.ASSIGN_TEACHER_TEMPLATE_NAME) RabbitTemplate assignTeacherRabbitTemplate;
    @Autowired
    private @Qualifier(RabbitMqConstant.ASSIGN_TEACHER_REPLY_TEMPLATE_NAME) RabbitTemplate assignTeacherReplyRabbitTemplate;
    @Autowired
    private @Qualifier(RabbitMqConstant.CREATE_GROUP_TEMPLATE_NAME) RabbitTemplate createGroupTemplate;
    @Autowired
    private @Qualifier(RabbitMqConstant.ASSIGN_FOREIGN_TEACHER_COMMENT_TEMPLATE_NAME) RabbitTemplate assignForeignTeacherCommentRabbitTemplate;


    public void send(Object object, QueueTypeEnum queueTypeEnum) {
        switch (queueTypeEnum) {
            case TEACHING_ONLINE: {
//                teachingOnlineRabbitTemplate.convertAndSend(object);
                break;
            }
            case TEACHING_SERVICE:
//                unsignedTeacherRabbitTemplate.convertAndSend(object);
                break;
            case NOTIFY_ORDER: {
                logger.info("@<-<-<-<-<-<-<-向订单中心发送状态通知;参数:{}",JacksonUtil.toJSon(object));
                notifyOrderTemplate.convertAndSend(object);
                break;
            }
            case NOTIFY_TIMER: {
                logger.info("@<-<-<-<-<-<-<-回复定时器,参数{}",JacksonUtil.toJSon(object));
                notifyTimerTemplate.convertAndSend(object);
                break;
            }
            case ASSIGN_TEACHER: {
                logger.debug("@<-<-<-<-<-<-<-向师生运营发送获取教师请求,参数{}", JacksonUtil.toJSon(object));
                assignTeacherRabbitTemplate.convertAndSend(object);
                break;
            }
            case ASSIGN_TEACHER_REPLY: {
                logger.debug("@<-<-<-<-<-<-<-教师分配回复,参数{}", JacksonUtil.toJSon(object));
                assignTeacherReplyRabbitTemplate.convertAndSend(object);
                break;
            }
            case CREATE_GROUP: {
                logger.info("@<-<-<-<-<-<-<-通知在线授课中心生成学生和教师group关系,参数:{}",JacksonUtil.toJSon(object));
                createGroupTemplate.convertAndSend(object);
                break;
            }
            case ASSIGN_TEACHER_TIMER_REPLY: {
                logger.info("@<-<-<-<-<-<-<-处理完定时任务的请求,参数:{}", JacksonUtil.toJSon(object));
                notifyTimerTemplate.convertAndSend(object);
            }
            case ASSIGN_FOREIGN_TEACHER_COMMENT:
                logger.debug("@<-<-<-<-<-<-<-向师生运营发送获取外教点评教师请求,参数{}",JacksonUtil.toJSon(object));
                assignForeignTeacherCommentRabbitTemplate.convertAndSend(JacksonUtil.toJSon(object));
            default:
                break;
        }
    }
}
