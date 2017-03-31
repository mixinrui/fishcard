package com.boxfishedu.workorder.servicex.home;

import com.boxfishedu.mall.enums.ProductType;
import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.accountcardinfo.OnlineAccountService;
import com.boxfishedu.workorder.web.result.StudentClassInfo;
import com.boxfishedu.workorder.web.result.StudentLeftInfo;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import org.hibernate.type.ClassType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private volatile StudentClassInfo emptyStudentClassInfo;

    private volatile StudentLeftInfo emptyStudentLeftInfo;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

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
            return this.buildEmptyClassInfo();
        } else {
            Long one2One = getOne2One(studentId, date);
            Long small = getSmallAmount(studentId, date);
            return new StudentClassInfo(one2One, small);
        }
    }

    private Long getSmallAmount(Long studentId, Date date) {
        Long small = courseScheduleRepository
                .studentOtherTypeClassInfoCurrentDay(
                        studentId, date, 1, ClassTypeEnum.SMALL.name(), FishCardStatusEnum.WAITFORSTUDENT.getCode());
        if (0l == small) {
            List<Service> services = serviceJpaRepository.findByStudentIdAndProductTypeAndComboType(studentId, ProductType.TEACHING.value(), ComboTypeEnum.SMALLCLASS.name());
            if (CollectionUtils.isEmpty(services)) {
                small = -1l;
            }
        }
        return small;
    }

    private Long getOne2One(Long studentId, Date date) {
        Long one2One = courseScheduleRepository
                .studentOne2OneClassInfoCurrentDay(
                        studentId, date, 1, FishCardStatusEnum.WAITFORSTUDENT.getCode());
        if (0l == one2One) {
            List<Service> services = serviceJpaRepository.findByStudentIdAndProductTypeAndComboTypeNot(studentId, ProductType.TEACHING.value(), ComboTypeEnum.SMALLCLASS.name());
            if (CollectionUtils.isEmpty(services)) {
                one2One = -1l;
            }
        }
        return one2One;
    }

    private StudentClassInfo buildEmptyClassInfo() {
        if (Objects.isNull(emptyStudentClassInfo)) {
            synchronized (this) {
                if (Objects.isNull(emptyStudentClassInfo)) {
                    logger.debug("@buildEmptyClassInfo#首次初始化emptyStudentClassInfo");
                    emptyStudentClassInfo = new StudentClassInfo(-1l, -1l);
                }
            }
        }
        return this.emptyStudentClassInfo;
    }

    private StudentLeftInfo buildEmptyStudentLeftInfo() {
        if (Objects.isNull(emptyStudentLeftInfo)) {
            synchronized (this) {
                if (Objects.isNull(emptyStudentLeftInfo)) {
                    logger.debug("@studentLeftInfo#首次初始化emptyStudentClassInfo");
                    emptyStudentLeftInfo = new StudentLeftInfo(0l, 0l, 0l, 0l);
                }
            }
        }
        return this.emptyStudentLeftInfo;
    }


    public StudentLeftInfo getLeftInfo(Long studentId) {
        if (!onlineAccountService.isMember(studentId)) {
            return this.buildEmptyStudentLeftInfo();
        }
        Long multiFRN = workOrderJpaRepository.multiLeftAmount(studentId, new Date(), ClassTypeEnum.SMALL.name());
        Long singleCN = workOrderJpaRepository.singleLeftAmount(studentId, new Date(), ClassTypeEnum.SMALL.name(), 1);
        Long singleFRN = workOrderJpaRepository.singleLeftAmount(studentId, new Date(), ClassTypeEnum.SMALL.name(), 2);
        Long comment = serviceJpaRepository.leftCommentAmount(studentId, ProductType.COMMENT.value());
        return new StudentLeftInfo(singleCN, singleFRN, comment.longValue(), comment);
    }
}
