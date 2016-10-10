package com.boxfishedu.workorder.common.rabbitmq;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatefulRetryOperationsInterceptorFactoryBean;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.HashMap;

@SuppressWarnings("ALL")
@Configuration
@EnableRabbit
public class RabbitMqConfiguration {
    public static final String NOTIFICATION_TASK_EXCHANGE = "boxfish.fishcard.sendMessage";
    public static final String ONLINE_TEACHING_EXCHANGE = "com.boxfishedu.onlineteaching.exchange";
    public static final String SCHEDULE_EXCHANGE = "boxfish.schedule";
    public static final String DELAY_QUEUE_EXCHANGE="boxfish.delay.exchange";
    public static final String DEAD_LETTER_EXCHANGE="x-dead-letter-exchange";
    public static final String FOREIGN_COMMENT_EXCHANGE="foreign-comment-exchange";
    public static final String DEAD_LETTER_EXCHANGE_ROUTING_KEY="x-dead-letter-routing-key";
    public static final String AMQ_DIRECT_EXCHANGE="amq.direct";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name= ConstantUtil.RABBITMQ_PROPERTIES_BEAN)
    private BoxFishRabbitProperties properties;

    @Bean(name = NOTIFICATION_TASK_EXCHANGE)
    public Exchange directExchange() {
        return new DirectExchange(NOTIFICATION_TASK_EXCHANGE, true, false);
    }

    @Bean(name = SCHEDULE_EXCHANGE)
    public Exchange scheduleExchange() {
        return new DirectExchange(SCHEDULE_EXCHANGE, true, false);
    }

    @Bean(name=DELAY_QUEUE_EXCHANGE)
    public Exchange delayExchange(){
        return new DirectExchange(DELAY_QUEUE_EXCHANGE, true, false);
    }

    @Bean(name=AMQ_DIRECT_EXCHANGE)
    public Exchange amqDirectExchange(){
        return new DirectExchange(AMQ_DIRECT_EXCHANGE, true, false);
    }

    @Primary
    @Bean(name = ONLINE_TEACHING_EXCHANGE)
    public Exchange directCreateGroupExchange() {
        return new DirectExchange(ONLINE_TEACHING_EXCHANGE, true, false);
    }

    @Bean(name=FOREIGN_COMMENT_EXCHANGE)
    public Exchange foreignCommentExchange(){
        return new DirectExchange(FOREIGN_COMMENT_EXCHANGE, true, false);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory, Exchange exchange) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        rabbitAdmin.declareExchange(directExchange());
        rabbitAdmin.declareExchange(directCreateGroupExchange());
        rabbitAdmin.declareExchange(scheduleExchange());
        rabbitAdmin.declareExchange(delayExchange());
        rabbitAdmin.declareExchange(foreignCommentExchange());


        /**
         *  发送短信队列
         */
        Queue notifyMessageQueue = new Queue(RabbitMqConstant.SHORT_MESSAGE_TEMPLATE_NAME, true);
        rabbitAdmin.declareQueue(notifyMessageQueue);
        Binding notifyMessageQueueBinding = BindingBuilder.bind(notifyMessageQueue).to(directExchange()).with(RabbitMqConstant.SHORT_MESSAGE_TEMPLATE_NAME).noargs();

        /**
         * 通知订单退款申请
         */
        Queue notifyRechargeWorkOrderQueue = new Queue(RabbitMqConstant.RECHARGE_WORKORDER_QUEUE, true);
        rabbitAdmin.declareQueue(notifyRechargeWorkOrderQueue);
        Binding notifyRechargeWorkOrderQueueBinding = BindingBuilder.bind(notifyRechargeWorkOrderQueue).to(directExchange()).with(RabbitMqConstant.RECHARGE_WORKORDER_QUEUE).noargs();

        /**
         * 通知定时器
         */
        Queue unassignedTeacherTimerReplyQueue = new Queue(RabbitMqConstant.UNASSIGNED_TEACHER_REPLY_TIMER_QUEUE, true);
        rabbitAdmin.declareQueue(unassignedTeacherTimerReplyQueue);
        Binding unassignedTeacherFailQueueBinding = BindingBuilder.bind(unassignedTeacherTimerReplyQueue).to(directExchange()).with(RabbitMqConstant.UNASSIGNED_TEACHER_REPLY_TIMER_QUEUE).noargs();

        /**
         * 通知订单中心
         */
        Queue notifyOrderQueue = new Queue(RabbitMqConstant.ORDER_NOTIFY_QUEUE, true);
        rabbitAdmin.declareQueue(notifyOrderQueue);
        Binding notifyOrderQueueBinding = BindingBuilder.bind(notifyOrderQueue).to(directExchange()).with(RabbitMqConstant.ORDER_NOTIFY_QUEUE).noargs();

        /**
         * 分配老师MQ
         */
        Queue assignTeacherQueue = new Queue(RabbitMqConstant.ASSIGNED_TEACHER_REQUEST_QUEUE, true);
        rabbitAdmin.declareQueue(assignTeacherQueue);
        Binding assignTeacherBinding = BindingBuilder.bind(assignTeacherQueue).to(scheduleExchange()).with(RabbitMqConstant.ASSIGNED_TEACHER_REQUEST_QUEUE).noargs();

        /**
         * 分配老师ReplyMQ
         */
        Queue assignTeacherReplyQueue = new Queue(RabbitMqConstant.ASSIGNED_TEACHER_REPLY_QUEUE, true);
        rabbitAdmin.declareQueue(assignTeacherReplyQueue);
        Binding assignTeacherReplyBinding = BindingBuilder.bind(assignTeacherReplyQueue).to(scheduleExchange()).with(RabbitMqConstant.ASSIGNED_TEACHER_REPLY_QUEUE).noargs();

        /**
         * 外教点评分配教师通知
         */
        Queue assignForeignTeacherCommentQueue = new Queue(RabbitMqConstant.ASSIGNED_FOREIGN_TEACHER_COMMENT_QUEUE, true);
        rabbitAdmin.declareQueue(assignForeignTeacherCommentQueue);
        Binding assignForeignTeacherCommentBinding = BindingBuilder.bind(assignForeignTeacherCommentQueue).to(foreignCommentExchange()).with(RabbitMqConstant.ASSIGNED_FOREIGN_TEACHER_COMMENT_QUEUE).noargs();

        /**
         * 响应外教点评分配教师
         */
        Queue allotForeignTeacherCommentQueue = new Queue(RabbitMqConstant.ALLOT_FOREIGN_TEACHER_COMMENT_QUEUE, true);
        rabbitAdmin.declareQueue(allotForeignTeacherCommentQueue);
        Binding allotForeignTeacherCommentBinding = BindingBuilder.bind(assignForeignTeacherCommentQueue).to(scheduleExchange()).with(RabbitMqConstant.ALLOT_FOREIGN_TEACHER_COMMENT_TEMPLATE_NAME).noargs();

        /**
         * 创建组
         */
        Queue createGroupQueue = new Queue(RabbitMqConstant.CREATE_GROUP_QUEUE, true);
        rabbitAdmin.declareQueue(createGroupQueue);
        Binding createGroupQueueBinding = BindingBuilder.bind(createGroupQueue).to(directCreateGroupExchange()).with(RabbitMqConstant.CREATE_GROUP_QUEUE).noargs();

        /**
         * 教师旷课延时
         */
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(DEAD_LETTER_EXCHANGE, "amq.direct");
        arguments.put(DEAD_LETTER_EXCHANGE_ROUTING_KEY, RabbitMqConstant.DELAY_TEACHER_DEALER_QUEUE);
        Queue delayTeacherAbsentQueue=new Queue(RabbitMqConstant.DELAY_TEACHER_ABSENT_QUEUE,true,false,false,arguments);
        rabbitAdmin.declareQueue(delayTeacherAbsentQueue);
        Binding delayQueueBinding=BindingBuilder.bind(delayTeacherAbsentQueue).to(delayExchange()).with(RabbitMqConstant.DELAY_TEACHER_ABSENT_QUEUE).and(arguments);

        /**
         * 教师旷课延时处理队列
         */
        Queue delayTeacherAbsentDealerQueue = new Queue(RabbitMqConstant.DELAY_TEACHER_DEALER_QUEUE, true);
        rabbitAdmin.declareQueue(delayTeacherAbsentDealerQueue);
        Binding delayTeacherAbsentDealerQueueBinding = BindingBuilder.bind(delayTeacherAbsentDealerQueue).to(amqDirectExchange()).with(RabbitMqConstant.DELAY_TEACHER_DEALER_QUEUE).noargs();

        /**
         * 学生旷课延时
         */
        HashMap<String, Object> studentAbsentarguments = new HashMap<String, Object>();
        studentAbsentarguments.put(DEAD_LETTER_EXCHANGE, "amq.direct");
        studentAbsentarguments.put(DEAD_LETTER_EXCHANGE_ROUTING_KEY, RabbitMqConstant.DELAY_STUDENT_DEALER_QUEUE);
        Queue delayStudentAbsentQueue=new Queue(RabbitMqConstant.DELAY_STUDENT_ABSENT_QUEUE,true,false,false,studentAbsentarguments);
        rabbitAdmin.declareQueue(delayStudentAbsentQueue);
        Binding delayStudentQueueBinding=BindingBuilder.bind(delayStudentAbsentQueue).to(delayExchange()).with(RabbitMqConstant.DELAY_STUDENT_ABSENT_QUEUE).and(studentAbsentarguments);

        /**
         * 学生旷课延时处理队列
         */
        Queue delayStudentAbsentDealerQueue = new Queue(RabbitMqConstant.DELAY_STUDENT_DEALER_QUEUE, true);
        rabbitAdmin.declareQueue(delayStudentAbsentDealerQueue);
        Binding delayStudentAbsentDealerQueueBinding = BindingBuilder.bind(delayStudentAbsentDealerQueue).to(amqDirectExchange()).with(RabbitMqConstant.DELAY_STUDENT_DEALER_QUEUE).noargs();

        /**
         * 服务端强制下课延时
         */
        HashMap<String, Object> forceCompleteArguments = new HashMap<String, Object>();
        forceCompleteArguments.put(DEAD_LETTER_EXCHANGE, "amq.direct");
        forceCompleteArguments.put(DEAD_LETTER_EXCHANGE_ROUTING_KEY, RabbitMqConstant.DELAY_COMPLETE_FORCE_DEALER_QUEUE);
        Queue delayForceCompleteQueue=new Queue(RabbitMqConstant.DELAY_COMPLETE_FORCE_QUEUE,true,false,false,forceCompleteArguments);
        rabbitAdmin.declareQueue(delayForceCompleteQueue);
        Binding delayForceCompleteQueueBinding=BindingBuilder.bind(delayForceCompleteQueue).to(delayExchange()).with(RabbitMqConstant.DELAY_COMPLETE_FORCE_QUEUE).and(forceCompleteArguments);

        /**
         * 服务端强制下课延时处理队列
         */
        Queue delayForceCompleteDealerQueue = new Queue(RabbitMqConstant.DELAY_COMPLETE_FORCE_DEALER_QUEUE, true);
        rabbitAdmin.declareQueue(delayForceCompleteDealerQueue);
        Binding delayForceCompleteDealerQueueBinding = BindingBuilder.bind(delayForceCompleteDealerQueue).to(amqDirectExchange()).with(RabbitMqConstant.DELAY_COMPLETE_FORCE_DEALER_QUEUE).noargs();

        /**
         * 通知教师上课延时队列
         */
        HashMap<String, Object> notifyTeacherPrepareArguments = new HashMap<String, Object>();
        notifyTeacherPrepareArguments.put(DEAD_LETTER_EXCHANGE, "amq.direct");
        notifyTeacherPrepareArguments.put(DEAD_LETTER_EXCHANGE_ROUTING_KEY, RabbitMqConstant.DELAY_NOTIFY_TEACHER_PREPARE_DEALER_QUEUE);
        Queue delayNotifyTeacherPrepareQueue=new Queue(RabbitMqConstant.DELAY__NOTIFY_TEACHER_PREPARE_QUEUE,true,false,false,notifyTeacherPrepareArguments);
        rabbitAdmin.declareQueue(delayNotifyTeacherPrepareQueue);
        Binding delayNotifyTeacherPrepareQueueBinding=BindingBuilder.bind(delayNotifyTeacherPrepareQueue).to(delayExchange()).with(RabbitMqConstant.DELAY__NOTIFY_TEACHER_PREPARE_QUEUE).and(notifyTeacherPrepareArguments);




        /**
         * 通知教师上课延时处理队列
         */
        Queue delayNotifyTeacherPrepareDealerQueue = new Queue(RabbitMqConstant.DELAY_NOTIFY_TEACHER_PREPARE_DEALER_QUEUE, true);
        rabbitAdmin.declareQueue(delayNotifyTeacherPrepareDealerQueue);
        Binding delayNotifyTeacherPrepareDealerQueueBinding;
        delayNotifyTeacherPrepareDealerQueueBinding = BindingBuilder.bind(delayNotifyTeacherPrepareDealerQueue).to(amqDirectExchange()).with(RabbitMqConstant.DELAY_NOTIFY_TEACHER_PREPARE_DEALER_QUEUE).noargs();

        /**
         * 通知外教点评卡修改学生或老师头像
         */
        Queue updatePictureQueue = new Queue(RabbitMqConstant.UPDATE_PICTURE_QUEUE, true);
        rabbitAdmin.declareQueue(updatePictureQueue);
        Binding updatePictureQueueBinding = BindingBuilder.bind(updatePictureQueue).to(directExchange()).with(RabbitMqConstant.UPDATE_PICTURE_QUEUE).noargs();

        /**
         * 同步鱼卡信息到客服系统
         */
        Queue syncFishCard2CustomerServiceQueue = new Queue(RabbitMqConstant.SYNC_FISHCARD_2_CUSTOMERSERVICE_QUEUE, true);
        rabbitAdmin.declareQueue(syncFishCard2CustomerServiceQueue);
        Binding syncFishCard2CustomerServiceQueueBinding = BindingBuilder.bind(syncFishCard2CustomerServiceQueue).to(directExchange()).with(RabbitMqConstant.SYNC_FISHCARD_2_CUSTOMERSERVICE_QUEUE).noargs();

        /**
         * 同步外教点评到客服系统
         */
        Queue syncCommentCard2CustomerServiceQueue = new Queue(RabbitMqConstant.SYNC_COMMENTCARD_2_CUSTOMERSERVICE_QUEUE, true);
        rabbitAdmin.declareQueue(syncCommentCard2CustomerServiceQueue);
        Binding syncCommentCard2CustomerServiceQueueBinding = BindingBuilder.bind(syncCommentCard2CustomerServiceQueue).to(foreignCommentExchange()).with(RabbitMqConstant.SYNC_COMMENTCARD_2_CUSTOMERSERVICE_QUEUE).noargs();


        rabbitAdmin.declareBinding(unassignedTeacherFailQueueBinding);
        rabbitAdmin.declareBinding(notifyOrderQueueBinding);
        rabbitAdmin.declareBinding(assignTeacherBinding);
        rabbitAdmin.declareBinding(assignForeignTeacherCommentBinding);
        rabbitAdmin.declareBinding(allotForeignTeacherCommentBinding);
        rabbitAdmin.declareBinding(assignTeacherReplyBinding);
        rabbitAdmin.declareBinding(createGroupQueueBinding);
        rabbitAdmin.declareBinding(delayQueueBinding);
        rabbitAdmin.declareBinding(delayTeacherAbsentDealerQueueBinding);
        rabbitAdmin.declareBinding(delayStudentQueueBinding);
        rabbitAdmin.declareBinding(delayStudentAbsentDealerQueueBinding);
        rabbitAdmin.declareBinding(delayForceCompleteQueueBinding);
        rabbitAdmin.declareBinding(delayForceCompleteDealerQueueBinding);
        rabbitAdmin.declareBinding(delayNotifyTeacherPrepareQueueBinding);
        rabbitAdmin.declareBinding(delayNotifyTeacherPrepareDealerQueueBinding);
        rabbitAdmin.declareBinding(updatePictureQueueBinding);
        rabbitAdmin.declareBinding(notifyRechargeWorkOrderQueueBinding);
        rabbitAdmin.declareBinding(notifyMessageQueueBinding); /** 短信 **/
        rabbitAdmin.declareBinding(syncFishCard2CustomerServiceQueueBinding);/**同步鱼卡信息到客服系统**/
        rabbitAdmin.declareBinding(syncCommentCard2CustomerServiceQueueBinding);/**同步外教点评到客服系统**/
        return rabbitAdmin;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        logger.info("rabbitmq======================address:[{}]",properties.getAddress());
        AbstractConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        factory.setAddresses(properties.getAddress());
//        factory.setHost(properties.getHost());
//        factory.setPort(properties.getPort());
        factory.setVirtualHost(properties.getVirtualHost());
        return factory;
    }

    @Bean
    public StatefulRetryOperationsInterceptor statefulRetryOperationsInterceptorFactoryBean(RabbitTemplate rabbitTemplate) {
        StatefulRetryOperationsInterceptorFactoryBean factoryBean = new StatefulRetryOperationsInterceptorFactoryBean();
        factoryBean.setMessageRecoverer(new MessageRecover(rabbitTemplate));
        factoryBean.setMessageKeyGenerator(message -> DigestUtils.md5DigestAsHex(message.getBody()));
        return factoryBean.getObject();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(MessageConverter messageConverter,
                                                                               StatefulRetryOperationsInterceptor interceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setMessageConverter(messageConverter());
        factory.setPrefetchCount(1);
        factory.setAdviceChain(interceptor);
        return factory;
    }


    private RabbitTemplate getRabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter, String smsQueueName) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        template.setQueue(smsQueueName);
        template.setRoutingKey(smsQueueName);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean(name = RabbitMqConstant.FINISH_FISHCARD_TEMPLATE_NAME)
    public RabbitTemplate teachingOnlineRabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.FISHCARD_CREATED_QUEUE);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        return template;
    }

    /**
     * 通知订单退款 template
     */
    @Bean(name = RabbitMqConstant.RECHARGE_WORKORDER_QUEUE_TEMPLATE_NAME)
    public RabbitTemplate notifyOrderRechargeTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.RECHARGE_WORKORDER_QUEUE);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        return template;
    }

    /**
     * 发送短信 template
     */
    @Bean(name = RabbitMqConstant.SHORT_MESSAGE_REPLY_TEMPLATE_NAME)
    public RabbitTemplate notifyMessageTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.SHORT_MESSAGE_TEMPLATE_NAME);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        return template;
    }



    /**
     *定时器回复template
     */
    @Bean(name = RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_REPLY_TEMPLATE_NAME)
    public RabbitTemplate unsignedTeacherReplyTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.UNASSIGNED_TEACHER_REPLY_TIMER_QUEUE);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        return template;
    }

    /**
     * 分配老师MQ
     * @param factory
     * @param messageConverter
     * @return
     */
    @Bean(name = RabbitMqConstant.ASSIGN_TEACHER_TEMPLATE_NAME)
    public RabbitTemplate assignTeacherRabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.ASSIGNED_TEACHER_REQUEST_QUEUE);
        template.setExchange(SCHEDULE_EXCHANGE);
        return template;
    }

    /**
     * 通知外教点评分配老师MQ
     * @param factory
     * @param messageConverter
     * @return
     */
    @Bean(name = RabbitMqConstant.ASSIGN_FOREIGN_TEACHER_COMMENT_TEMPLATE_NAME)
    public RabbitTemplate assignForeignTeacherCommentTemplate(ConnectionFactory factory, MessageConverter messageConverter){
        RabbitTemplate template = getRabbitTemplate(factory,messageConverter,RabbitMqConstant.ASSIGNED_FOREIGN_TEACHER_COMMENT_QUEUE);
        template.setExchange(FOREIGN_COMMENT_EXCHANGE);
        return template;
    }

    /**
     * 响应外教点评分配老师MQ 
     * @param factory 
     * @param messageConverter 
     * @return 
     */
    @Bean(name = RabbitMqConstant.ALLOT_FOREIGN_TEACHER_COMMENT_TEMPLATE_NAME)
    public RabbitTemplate allotTeacherReplyRabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter){
        RabbitTemplate template = getRabbitTemplate(connectionFactory,messageConverter,RabbitMqConstant.ALLOT_FOREIGN_TEACHER_COMMENT_QUEUE);
        template.setExchange(SCHEDULE_EXCHANGE);
        return template;
    }

    @Bean(name = RabbitMqConstant.ASSIGN_TEACHER_REPLY_TEMPLATE_NAME)
    public RabbitTemplate assignTeacherReplyRabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.ASSIGNED_TEACHER_REPLY_QUEUE);
        template.setExchange(SCHEDULE_EXCHANGE);
        return template;
    }

    /**
     *创建群template
     */
    @Bean(name = RabbitMqConstant.CREATE_GROUP_TEMPLATE_NAME)
    public RabbitTemplate createGroupRabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.CREATE_GROUP_QUEUE);
        template.setExchange(ONLINE_TEACHING_EXCHANGE);
        return template;
    }


    /**
     *订单状态通知
     */
    @Primary
    @Bean(name = RabbitMqConstant.ORDER_NOTIFY_TEMPALTE_NAME)
    public RabbitTemplate notifyOrderRabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.ORDER_NOTIFY_QUEUE);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        return template;
    }

    /**
     *教师旷课延迟队列template
     */
    @Bean(name = RabbitMqConstant.DELAY_QUEUE_TEACHER_ABSENT_TEMPLATE_NAME)
    public RabbitTemplate delayTeacherAbsentQueueTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.DELAY_TEACHER_ABSENT_QUEUE);
        template.setExchange(DELAY_QUEUE_EXCHANGE);
        return template;
    }

    /**
     *学生旷课延迟队列template
     */
    @Bean(name = RabbitMqConstant.DELAY_QUEUE_STUDENT_ABSENT_TEMPLATE_NAME)
    public RabbitTemplate delayStudentAbsentQueueTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.DELAY_STUDENT_ABSENT_QUEUE);
        template.setExchange(DELAY_QUEUE_EXCHANGE);
        return template;
    }

    /**
     *强制下课延迟template
     */
    @Bean(name = RabbitMqConstant.DELAY_QUEUE_FORCE_COMPLETE_TEMPLATE_NAME)
    public RabbitTemplate delayCompleteForceQueueTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.DELAY_COMPLETE_FORCE_QUEUE);
        template.setExchange(DELAY_QUEUE_EXCHANGE);
        return template;
    }

    /**
     *通知教师做上课准备
     */
    @Bean(name = RabbitMqConstant.DELAY_QUEUE_NOTIFY_TEACHER_PREPARE_TEMPLATE_NAME)
    public RabbitTemplate delayTeacherPrepareTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.DELAY__NOTIFY_TEACHER_PREPARE_QUEUE);
        template.setExchange(DELAY_QUEUE_EXCHANGE);
        return template;
    }


    /**
     *通知订单退款
     */
    @Bean(name = RabbitMqConstant.RECHARGE_WORKORDER_QUEUE_TEMPLATE_NAME)
    public RabbitTemplate notifyOrderPrepareTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.RECHARGE_WORKORDER_QUEUE);
        template.setExchange(DELAY_QUEUE_EXCHANGE);
        return template;
    }

    /**
     *通知订单退款
     */
    @Bean(name = RabbitMqConstant.SHORT_MESSAGE_REPLY_TEMPLATE_NAME)
    public RabbitTemplate syncFishCard2CustomerServiceTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.SHORT_MESSAGE_TEMPLATE_NAME);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        return template;
    }

    /**
     *同步鱼卡数据到客服系统
     */
    @Bean(name = RabbitMqConstant.SYNC_FISHCARD_2_CUSTOMERSERVICE_TEMPLATE_NAME)
    public RabbitTemplate notifyMessagePrepareTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.SYNC_FISHCARD_2_CUSTOMERSERVICE_QUEUE);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        return template;
    }

    /**
     *同步外教点评到客服系统
     */
    @Bean(name = RabbitMqConstant.SYNC_COMMENTCARD_2_CUSTOMERSERVICE_TEMPLATE_NAME)
    public RabbitTemplate syncCommentCard2CustomerServiceTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.SYNC_COMMENTCARD_2_CUSTOMERSERVICE_TEMPLATE_NAME);
        template.setExchange(FOREIGN_COMMENT_EXCHANGE);
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private static class MessageRecover extends RejectAndDontRequeueRecoverer {
        private final RabbitTemplate rabbitTemplate;

        public MessageRecover(RabbitTemplate rabbitTemplate) {
            this.rabbitTemplate = rabbitTemplate;
        }

        @Override
        public void recover(Message message, Throwable cause) {
            logger.error(message, cause);
            rabbitTemplate.send(message);
            super.recover(message, cause);
        }
    }
}
