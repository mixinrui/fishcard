package com.boxfishedu.fishcard.timer.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatefulRetryOperationsInterceptorFactoryBean;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.util.DigestUtils;

@SuppressWarnings("ALL")
@Configuration
@EnableRabbit
public class RabbitMqConfiguration {
    public static final String NOTIFICATION_TASK_EXCHANGE = "boxfish.fishcard.sendMessage";
    public static final String NOTIFICATION_TIMER_RPC_EXCHANGE="boxfish.fishcard.timer.rpc.sendMessage";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${spring.rabbitmq.address}")
    private String address;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${spring.rabbitmq.virtualHost}")
    private String virtualHost;


    @Bean(name = NOTIFICATION_TASK_EXCHANGE)
    public Exchange directExchange() {
        return new DirectExchange(NOTIFICATION_TASK_EXCHANGE, true, false);
    }

   @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory, Exchange exchange) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        rabbitAdmin.declareExchange(directExchange());

        Queue unsignedTeacherTimerQueue= new Queue(RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_QUEUE, true);
        rabbitAdmin.declareQueue(unsignedTeacherTimerQueue);
        Binding unsgingerTeacherTimerBinding = BindingBuilder.bind(unsignedTeacherTimerQueue).to(directExchange()).with(RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_QUEUE).noargs();

        rabbitAdmin.declareBinding(unsgingerTeacherTimerBinding);
        return rabbitAdmin;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("localhost");
        factory.setUsername(username);
        factory.setPassword(password);
//        factory.setHost(host);
//        factory.setPort(port);
        factory.setAddresses(address);
        factory.setVirtualHost(virtualHost);
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
//        factory.setMessageConverter(messageConverter);
        factory.setPrefetchCount(1);
        factory.setAdviceChain(interceptor);
        return factory;
    }

    @Primary
    @Bean(name=RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_TEMPLATE_NAME)
    public RabbitTemplate unsignedTeacherTimerRabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter) {
        RabbitTemplate template = getRabbitTemplate(factory, messageConverter, RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_QUEUE);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        return template;
    }

    private RabbitTemplate getRabbitTemplate(ConnectionFactory factory, MessageConverter messageConverter, String smsQueueName) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setExchange(NOTIFICATION_TASK_EXCHANGE);
        template.setQueue(smsQueueName);
        template.setRoutingKey(smsQueueName);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private static class MessageRecover extends RejectAndDontRequeueRecoverer {
        private final  RabbitTemplate rabbitTemplate;

        public MessageRecover(RabbitTemplate rabbitTemplate) {
            this.rabbitTemplate = rabbitTemplate;
        }

        @Override
        public void recover(Message message, Throwable cause) {
            logger.error(message,cause);
            rabbitTemplate.send(message);
            super.recover(message, cause);
        }
    }
}
