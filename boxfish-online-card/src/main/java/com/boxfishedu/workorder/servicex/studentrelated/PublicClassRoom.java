package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.bean.PublicClassMessageEnum;
import com.boxfishedu.workorder.common.exception.PublicClassException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.PublicClassInfo;
import com.boxfishedu.workorder.entity.mysql.PublicClassInfoJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Created by LuoLiBing on 17/1/9.
 * 公开课堂
 */
@Component
public class PublicClassRoom {

    private static PublicClassInfoJpaRepository publicClassInfoJpaRepository;

    private static ServiceSDK serviceSdk;

    @Autowired
    public void init(PublicClassInfoJpaRepository classInfoJpaRepository, ServiceSDK ss) {
        publicClassInfoJpaRepository = classInfoJpaRepository;
        serviceSdk = ss;
    }

    public void enter(SmallClass smallClass, Long studentId, String accessToken) {
        // 是否是在正常的时间点

        // 一 判断是否曾经进入过这个房间, 是则直接进入
        if(isOnceEntered(smallClass.getId(), studentId)) {
            return;
        }

        // 二 判断会员属性, 判断上公开课的次数
        checkMember(smallClass, studentId, accessToken);

        // 三 如果没有消费过, 则直接进入房间, 保存
        savePublicClassInfo(smallClass, studentId);
    }

    private void savePublicClassInfo(SmallClass smallClass, Long studentId) {
        PublicClassInfo entity = new PublicClassInfo();
        entity.setClassDate(DateUtil.convertLocalDate(smallClass.getClassDate()));
        entity.setSlotId(smallClass.getSlotId());
        entity.setSmallClassId(smallClass.getId());
        entity.setStudentId(studentId);
        publicClassInfoJpaRepository.save(entity);
    }

    private boolean isOnceEntered(Long smallClassId, Long studentId) {
        Optional<PublicClassInfo> pciOptional = publicClassInfoJpaRepository.findBySmallClassIdAndStudentId(
                smallClassId, studentId);
        return (pciOptional.isPresent());
    }

    private void checkMember(SmallClass smallClass, Long studentId, String accessToken) {
        // 获取会员信息
        JsonResultModel resultModel = serviceSdk.getMemberInfo(accessToken);
        Map memberInfo = (Map) resultModel.getData();
        String memberType;
        // 如果获取不到会员类型, 默认为非会员
        if(memberInfo.get("type") == null) {
            memberType = "NONE";
        } else {
            memberType = memberInfo.get("type").toString();
        }
        LocalDate classDate = DateUtil.convertLocalDate(smallClass.getClassDate());
        switch (memberType) {
            // 非会员验证
            case "NONE" : nonMemberCheck(classDate, studentId); break;
            // 会员验证
            default: memberCheck(classDate, studentId); break;
        }
    }

    /**
     * 非会员验证
     * @param classDate
     * @param studentId
     */
    private void nonMemberCheck(LocalDate classDate, Long studentId) {
        // 非会员, 每周只能上一节课
        Integer count = publicClassInfoJpaRepository.findByClassDateRangeAndStudentId(
                DateUtil.getFirstDateOfWeek(classDate),
                DateUtil.getLastDateOfWeek(classDate),
                studentId);
        if(count > 0) {
            throw new PublicClassException(PublicClassMessageEnum.NON_MEMBER);
        }
    }

    /**
     * 会员验证
     */
    private void memberCheck(LocalDate classDate, Long studentId) {
        // 会员每天只能上一节课
        Integer count = publicClassInfoJpaRepository.findByClassDateAndStudentId(classDate, studentId);
        if(count > 0) {
            throw new PublicClassException(PublicClassMessageEnum.EVERY_DAY_LIMIT);
        }
    }
}
