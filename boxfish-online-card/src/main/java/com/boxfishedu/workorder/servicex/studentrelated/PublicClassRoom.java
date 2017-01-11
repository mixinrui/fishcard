package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
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
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Created by LuoLiBing on 17/1/9.
 * 公开课堂
 */
@Service
public class PublicClassRoom {

    // 进入过课堂的人
    private final static String CLASS_ROOM_MEMBER_KEY = "publicClassRoom_member_";

    // 一周以内学过的学生
    private final static String CLASS_ROOM_MEMBER_WEEK_KEY = "publicClassRoom_member_week_";

    // 一天以内进入过的学生
    private final static String CLASS_ROOM_MEMBER_DAY_KEY = "publicClassRoom_member_day_";

    // 课堂实时人数
    private final static String CLASS_ROOM_MEMBER_REAL_TIME = "publicClassRoom_member_real_time_";

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

        // 一 判断是否是第一次进入这个课堂, 如果不是, 则直接返回
        if(isOnceEntered(smallClass.getId(), studentId)) {
            // 更改状态
            updateEnterStatus(smallClass.getId(), studentId);
            return;
        }

        // 二 判断会员属性, 判断上公开课的次数
        checkMember(smallClass, studentId, accessToken);

        // 三 如果没有消费过, 则直接进入房间, 保存
        savePublicClassInfo(smallClass, studentId);

        // 四 更新redis
        updateEnterClassStatisticsCache(smallClass, studentId);
    }


    /**
     * 退出课堂
     * @param smallClassId
     * @param studentId
     */
    @Transactional
    public void quit(Long smallClassId, Long studentId) {
        publicClassInfoJpaRepository.updateStatus(PublicClassInfoStatusEnum.QUIT.code, smallClassId, studentId);
        setOperations.remove(CLASS_ROOM_MEMBER_REAL_TIME + smallClassId, studentId);
    }


    /**
     * 实时获取课堂人数
     * @param smallClassId
     * @return
     */
    public long getClassRoomStudentCount(Long smallClassId) {
        return setOperations.size(CLASS_ROOM_MEMBER_REAL_TIME + smallClassId);
    }

    private void savePublicClassInfo(SmallClass smallClass, Long studentId) {
        PublicClassInfo entity = new PublicClassInfo();
        entity.setClassDate(DateUtil.convertLocalDate(smallClass.getClassDate()));
        entity.setSlotId(smallClass.getSlotId());
        entity.setSmallClassId(smallClass.getId());
        entity.setStudentId(studentId);
        entity.setStatus(PublicClassInfoStatusEnum.ENTER.code);
        publicClassInfoJpaRepository.save(entity);
        // 更新课堂实时缓存
        updateEnterCacheRealTime(smallClass.getId(), studentId);
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

        // 先判断这周是否已经上过课, 如果没有, 可以直接进入课堂, 否则, 再判断是否是会员, 今天是否上课
        if(!setOperations.isMember(createClassRoomWeekKey(smallClass.getClassDate()), studentId)) {
            return;
        }

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
            // 非会员验证, 这周已经上过课, 提示不能上课
            case "NONE" : throw new PublicClassException(PublicClassMessageEnum.NON_MEMBER);
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
        Boolean haveCourse = setOperations.isMember(CLASS_ROOM_MEMBER_DAY_KEY + DateUtil.dateFormatter.format(classDate), studentId);
        if(haveCourse) {
            throw new PublicClassException(PublicClassMessageEnum.EVERY_DAY_LIMIT);
        }

        // 会员每天只能上一节课
//        Integer count = publicClassInfoJpaRepository.findByClassDateAndStudentId(classDate, studentId);
//        if(count > 0) {
//            throw new PublicClassException(PublicClassMessageEnum.EVERY_DAY_LIMIT);
//        }
    }


    /**
     * 更新状态
     * @param smallClassId
     * @param studentId
     */
    private void updateEnterStatus(Long smallClassId, Long studentId) {
        publicClassInfoJpaRepository.updateStatus(PublicClassInfoStatusEnum.ENTER.code, smallClassId, studentId);
        // 更新课堂实时缓存
        updateEnterCacheRealTime(smallClassId, studentId);
    }


    private void updateEnterCacheRealTime(Long smallClassId, Long studentId) {
        setOperations.add(CLASS_ROOM_MEMBER_REAL_TIME + smallClassId, studentId);
    }


    /**
     * 更新进入课堂统计分析缓存
     * @param smallClass
     * @param studentId
     */
    private void updateEnterClassStatisticsCache(SmallClass smallClass, Long studentId) {
        // 将学生加入到这个课堂当中,下次直接判断是否在缓存中
        setOperations.add(CLASS_ROOM_MEMBER_KEY + smallClass.getId(), studentId);
        // 加入到这一天的上课当中.
        setOperations.add(CLASS_ROOM_MEMBER_DAY_KEY + DateUtil.simpleDate2String(smallClass.getClassDate()), studentId);
        // 加入到一周上课学生列表当中.
        setOperations.add(createClassRoomWeekKey(smallClass.getClassDate()), studentId);
    }

    private String createClassRoomWeekKey(Date classDate) {
        LocalDate firstDateOfWeek = DateUtil.getFirstDateOfWeek(DateUtil.convertLocalDate(classDate));
        return CLASS_ROOM_MEMBER_WEEK_KEY + DateUtil.dateFormatter.format(firstDateOfWeek);
    }
}
