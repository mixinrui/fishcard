package com.boxfishedu.workorder.servicex.instantclass.timer;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqDelaySender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.sun.media.jfxmedia.logging.Logger;
import org.slf4j.LoggerFactory;
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

    private org.slf4j.Logger logger= LoggerFactory.getLogger(this.getClass());

    public void putCardsToMatchTeachers() {
        //TODO:此处需要使用配置
        LocalDateTime beginLocal = LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(20 + 10);

        List<InstantClassCard> instantClassCards = instantClassJpaRepository.findByRequestMatchTeacherTimeBetweenAndStatusIn(
                DateUtil.localDate2Date(beginLocal), new Date(), new Integer[]{InstantClassRequestStatus.WAIT_TO_MATCH.getCode()});
        logger.debug("====排序前===>>>>>:begin:{}",DateUtil.localDate2Date(beginLocal));
        instantClassCards.forEach(instantClassCard1 -> logger.debug(instantClassCard1.toString()));
        instantClassCards.sort(Comparator.comparing(instantClassCard->instantClassCard.getRequestMatchTeacherTime().getTime()));
        logger.debug("====排序后===<<<<<");
        instantClassCards.forEach(instantClassCard1 -> logger.debug(instantClassCard1.toString()));

        instantClassCards.forEach(instantClassCard -> rabbitMqDelaySender
                .sendInstantClassMsg(instantClassCard,InstantClassMessageProperties.getMsgProperties(instantClassCard)));

    }

    public void markUnmatchCard(){
        //TODO:需要使用配置
        LocalDateTime endLocal = LocalDateTime.now(ZoneId.systemDefault()).minusSeconds(70);
        LocalDateTime beginLocal = endLocal.minusHours(1);
        List<InstantClassCard> instantClassCards = instantClassJpaRepository.findByRequestMatchTeacherTimeBetweenAndStatusIn(
                DateUtil.localDate2Date(beginLocal), DateUtil.localDate2Date(endLocal), new Integer[]{InstantClassRequestStatus.WAIT_TO_MATCH.getCode()});
        logger.warn("超过一分钟未匹配教师,通过定时器强制设置为未匹配的抢单卡",instantClassCards);
        instantClassCards.forEach(instantClassCard -> instantClassJpaRepository.updateStatus(instantClassCard.getId(),InstantClassRequestStatus.NO_MATCH.getCode()));

    }
}
