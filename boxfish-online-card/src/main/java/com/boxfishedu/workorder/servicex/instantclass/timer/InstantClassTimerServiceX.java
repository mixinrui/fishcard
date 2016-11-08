package com.boxfishedu.workorder.servicex.instantclass.timer;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqDelaySender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/11/8.
 */
@Component
public class InstantClassTimerServiceX {
    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private RabbitMqDelaySender rabbitMqDelaySender;

    public void putCardsToMatchTeachers() {
        //TODO:此处需要使用配置
        LocalDateTime beginLocal = LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(20 + 5);

        List<InstantClassCard> instantClassCards = instantClassJpaRepository.findByRequestMatchTeacherTimeBetweenAndStatusIn(
                DateUtil.localDate2Date(beginLocal), new Date(), new Integer[]{InstantClassRequestStatus.WAIT_TO_MATCH.getCode()});
        System.out.println("====排序前===>>>>>");
        instantClassCards.forEach(instantClassCard1 -> System.out.println(instantClassCard1));
        instantClassCards.sort(Comparator.comparing(instantClassCard->instantClassCard.getRequestMatchTeacherTime().getTime()));
        System.out.println("====排序后===<<<<<");
        instantClassCards.forEach(instantClassCard1 -> System.out.println(instantClassCard1));

        instantClassCards.forEach(instantClassCard -> rabbitMqDelaySender
                .sendInstantClassMsg(instantClassCard,InstantClassMessageProperties.getMsgProperties(instantClassCard)));

    }
}
