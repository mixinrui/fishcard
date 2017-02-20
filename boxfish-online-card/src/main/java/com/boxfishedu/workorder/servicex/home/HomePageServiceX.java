package com.boxfishedu.workorder.servicex.home;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.servicex.studentrelated.validator.RepeatedSubmissionChecker;
import com.boxfishedu.workorder.web.result.StudentClassInfo;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by hucl on 16/11/30.
 */
@Component
public class HomePageServiceX {
    @Autowired
    private AccountCardInfoService accountCardInfoService;
    @Autowired
    private OnlineAccountService onlineAccountService;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    private StudentClassInfo emptyStudentClassInfo;

    @Autowired
    CourseScheduleRepository courseScheduleRepository;

    private final String PUBLIC_CLASS_KEY_PREFIX = "public_class:";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public JsonResultModel getHomePage(String order_type, @PathVariable("student_id") Long studentId) {
        if (!onlineAccountService.isMember(studentId)) {
            logger.debug("@userCardInfo#{}不是在线购买用户,直接返回", studentId);
            return JsonResultModel.newJsonResultModel(AccountCardInfo.buildEmpty());
        }
        AccountCardInfo accountCardInfo = accountCardInfoService.queryByStudentId(studentId);
        if (null != order_type) {
            switch (order_type) {
                case "chinese":
                    accountCardInfo.setComment(null);
                    accountCardInfo.setForeign(null);
                    break;
                case "foreign":
                    accountCardInfo.setComment(null);
                    accountCardInfo.setChinese(null);
                    break;
                case "comment":
                    accountCardInfo.setForeign(null);
                    accountCardInfo.setChinese(null);
                    break;
                default:
                    break;
            }
        }
        return JsonResultModel.newJsonResultModel(accountCardInfo);
    }


    public JsonResultModel getPublicHomePage() {
        List<AccountCourseBean.CardCourseInfo> cardCourseInfos = getPublicCourseInfosFromDb();

        return JsonResultModel.newJsonResultModel(cardCourseInfos);
    }

    public void putIntoRedis() {

    }

    private List<AccountCourseBean.CardCourseInfo> getPublicCourseInfosFromDb() {
        List<SmallClass> smallClasses =
                smallClassJpaRepository.findByClassDateAndClassType(new Date(), ClassTypeEnum.PUBLIC.name());
        List<AccountCourseBean.CardCourseInfo> cardCourseInfos = Lists.newArrayList();
        smallClasses.forEach(smallClass -> {
            AccountCourseBean.CardCourseInfo cardCourseInfo = new AccountCourseBean.CardCourseInfo();
            cardCourseInfo.setSmallClassId(smallClass.getId());
            cardCourseInfo.setSmallClassInfo(smallClass);
            cardCourseInfo.setStatus(smallClass.getStatus());
            cardCourseInfo.setThumbnail(smallClass.getCover());
            cardCourseInfo.setCourseType(smallClass.getCourseType());
            cardCourseInfo.setCourseId(smallClass.getCourseId());
            cardCourseInfo.setCourseName(smallClass.getCourseName());
            cardCourseInfo.setDateInfo(smallClass.getStartTime());
            cardCourseInfo.setDifficulty(smallClass.getDifficultyLevel());
            cardCourseInfos.add(cardCourseInfo);
        });
        return cardCourseInfos;
    }

    public JsonResultModel studentClassInfo(Long studentId, Date date) {
        return JsonResultModel.newJsonResultModel(this.getStudentClassInfo(studentId, date));
    }

    public StudentClassInfo getStudentClassInfo(Long studentId, Date date) {
        if (!onlineAccountService.isMember(studentId)) {
            return this.buildEmptyClassInfo(studentId);
        } else {
            Long one2One = courseScheduleRepository
                    .studentOne2OneClassInfoCurrentDay(
                            studentId, date, 1, FishCardStatusEnum.WAITFORSTUDENT.getCode());
            Long small = courseScheduleRepository
                    .studentOtherTypeClassInfoCurrentDay(
                            studentId, date, 1, ClassTypeEnum.SMALL.name(), FishCardStatusEnum.WAITFORSTUDENT.getCode());
            return new StudentClassInfo(one2One, small);
        }
    }

    private StudentClassInfo buildEmptyClassInfo(Long studentId) {
        if (Objects.isNull(emptyStudentClassInfo)) {
            synchronized (this) {
                logger.debug("@buildEmptyClassInfo#首次初始化emptyStudentClassInfo");
                emptyStudentClassInfo = new StudentClassInfo(0l, 0l);
            }
        }
        return this.emptyStudentClassInfo;
    }


}
