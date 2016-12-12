package com.boxfishedu.workorder.servicex.instantclass.timer;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.service.instantclass.InstantClassTeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by hucl on 16/11/8.
 */
@Component
public class InstantClassTimerDealer {
    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Autowired
    private InstantClassTeacherService instantClassTeacherService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    public void timerGetInstantTeachers(InstantClassCard instantClassCard) {
        InstantClassCard dbInstantCard = instantClassJpaRepository.findForUpdate(instantClassCard.getId());
        if (dbInstantCard.getRequestTeacherTimes() != instantClassCard.getRequestTeacherTimes()) {
            logger.debug("@timerGetInstantTeachers#repeat# IIIIIIIIIIIIIII 已经请求过一次教师,放弃该消息#[{}]", JacksonUtil.toJSon(instantClassCard));
            return;
        }
        if (dbInstantCard.getStatus() == InstantClassRequestStatus.WAIT_TO_MATCH.getCode()) {
            //TODO:3应该写成配置
            if (dbInstantCard.getRequestTeacherTimes() % 3 == 0) {
                logger.debug("@timerGetInstantTeachers#unmatch# IIIIIIIIIIIIIII 三次轮询没有匹配,更新结果为未匹配[{}]", JacksonUtil.toJSon(instantClassCard));
                dbInstantCard.setStatus(InstantClassRequestStatus.NO_MATCH.getCode());
                dbInstantCard.setUpdateTime(new Date());
                instantClassJpaRepository.save(dbInstantCard);
                return;
            }
        }
        if (dbInstantCard.getStatus() == InstantClassRequestStatus.NO_MATCH.getCode()) {
            logger.debug("@timerGetInstantTeachers#unmatch# IIIIIIIIIIIIIII 已被标记为无匹配,card#[{}],返回", JacksonUtil.toJSon(instantClassCard));
            return;
        }
        if (dbInstantCard.getStatus() == InstantClassRequestStatus.MATCHED.getCode()) {
            logger.debug("@timerGetInstantTeachers#matched# IIIIIIIIIIIIIII 已被标记为匹配,card#[{}],返回", JacksonUtil.toJSon(instantClassCard));
            return;
        }
        instantClassTeacherService.dealFetchedTeachersAsync(dbInstantCard);
    }
}
