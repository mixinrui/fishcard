package com.boxfishedu.workorder.servicex.instantclass.timer;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqDelaySender;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    @Autowired
    private ThreadPoolManager threadPoolManager;

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
        LocalDateTime beginLocal = LocalDateTime.now(ZoneId.systemDefault()).minusHours(1);
        List<InstantClassCard> instantClassCards = instantClassJpaRepository.findByRequestMatchTeacherTimeBetweenAndStatusIn(
                DateUtil.localDate2Date(beginLocal), DateUtil.localDate2Date(endLocal), new Integer[]{InstantClassRequestStatus.WAIT_TO_MATCH.getCode()});
        logger.warn("@markUnmatchCard超过一分钟未匹配教师,通过定时器强制设置为未匹配的抢单卡",instantClassCards);
        instantClassCards.forEach(instantClassCard -> instantClassJpaRepository.updateStatus(instantClassCard.getId(),InstantClassRequestStatus.NO_MATCH.getCode()));

    }

    public void backUnmatchCoursesAsync(){
        threadPoolManager.execute(new Thread(()->{
            this.backUnmatchCourses();
        }));
    }

    public void backUnmatchCourses() {
        Date deadLine=DateUtil.localDate2Date(LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(35));
        List<InstantClassCard> instantCards=instantClassJpaRepository.findByCreateTimeLessThanAndWorkOrderId(deadLine,null);
        if(CollectionUtils.isEmpty(instantCards)){
            return;
        }
        instantCards.forEach(instantCard->{
            logger.debug("@backUnmatchCourses,向课程推荐发送取消课程,学生[{}],instantcard[{}]",instantCard.getStudentId(),instantCard.getId());
            recommandCourseRequester.cancelUnmatchedInstantCourse(instantCard);
        });
    }
}
