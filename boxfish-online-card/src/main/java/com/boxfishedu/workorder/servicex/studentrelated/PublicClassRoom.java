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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Created by LuoLiBing on 17/1/9.
 * 公开课堂
 */
@Service
public class PublicClassRoom {

    private final static String CLASS_ROOM_MEMBER_KEY = "classRoom_member_";

    @Autowired
    private PublicClassInfoJpaRepository publicClassInfoJpaRepository;

    @Autowired
    private ServiceSDK serviceSdk;

    private SetOperations<String, Long> setOperations;

    @Autowired
    public void initRedis(@Qualifier(value = "stringLongRedisTemplate") RedisTemplate<String, Long> redisTemplate) {
        setOperations = redisTemplate.opsForSet();
    }

    @Transactional
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

        // 四 更新redis
        updateCache(smallClass, studentId);
    }

    private void savePublicClassInfo(SmallClass smallClass, Long studentId) {
        PublicClassInfo entity = new PublicClassInfo();
        entity.setClassDate(DateUtil.convertLocalDate(smallClass.getClassDate()));
        entity.setSlotId(smallClass.getSlotId());
        entity.setSmallClassId(smallClass.getId());
        entity.setStudentId(studentId);
        publicClassInfoJpaRepository.save(entity);
    }

    /**
     * 判断是否进入过房间
     * @param smallClassId
     * @param studentId
     * @return
     */
    private boolean isOnceEntered(Long smallClassId, Long studentId) {
        try {
            // 优先使用缓存, 只有在缓存出错的情况下才使用数据库查询
            return setOperations.isMember(CLASS_ROOM_MEMBER_KEY + smallClassId, studentId);
        } catch (Exception e) {
            Optional<PublicClassInfo> pciOptional = publicClassInfoJpaRepository.findBySmallClassIdAndStudentId(
                    smallClassId, studentId);
            return (pciOptional.isPresent());
        }
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

    /**
     * 更新缓存
     * @param classRoom
     * @param studentId
     */
    private void updateCache(SmallClass classRoom, Long studentId) {
        // 将学生加入到这个课堂当中,下次直接判断是否在缓存中
        setOperations.add(CLASS_ROOM_MEMBER_KEY + classRoom.getId(), studentId);
    }


}
