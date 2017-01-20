package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.bean.CourseDifficultyEnum;
import com.boxfishedu.workorder.common.bean.PublicClassMessageEnum;
import com.boxfishedu.workorder.common.bean.PublicClassTimeEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.PublicClassException;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by LuoLiBing on 17/1/18.
 */
@Service
public class PublicClassService {

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private PublicClassRoom publicClassRoom;

    private Cache publicClassCacheWithLevelAndDate;

    private Cache publicClassCacheWithId;

    @Autowired
    public void initCache(CacheManager cacheManager) {
        publicClassCacheWithLevelAndDate = cacheManager.getCache(
                CacheKeyConstant.PUBLIC_CLASS_ROOM_WITH_LEVELANDDATE);
        publicClassCacheWithId = cacheManager.getCache(
                CacheKeyConstant.PUBLIC_CLASS_ROOM_WITH_ID);
    }

    /**
     * 获取公开课课表
     * @param level
     * @param now
     * @return
     */
    public Map<String, Object> getStudentPublicClassTimeEnum(String level, LocalDate now) {
        String key = createPublicClassCacheWithLevelAndDateKey(level, now);
        Map classRoom = publicClassCacheWithLevelAndDate.get(key, Map.class);
        if(classRoom == null) {
            synchronized (this) {
                classRoom = getClassRoomByLevelAndNowWithDatabase(level, now);
                publicClassCacheWithLevelAndDate.putIfAbsent(key, classRoom);
            }
        }
        return classRoom;

    }


    public void evictPublicClassRoom(String level) {
        publicClassCacheWithLevelAndDate.evict(createPublicClassCacheWithLevelAndDateKey(level, LocalDate.now()));
    }

    private String createPublicClassCacheWithLevelAndDateKey(String level, LocalDate now) {
        return DateUtil.dateFormatter.format(now) + "_" + level;
    }


    /**
     * 学生进入公开课课堂
     * @return
     */
    public Map<String, Object> enterPublicClassRoom(Long studentId, String nickName, Long smallClassId, String accessToken) {
        try {
            SmallClass smallClass = getClassRoomById(smallClassId);
            publicClassRoom.enter(smallClass, studentId, nickName, accessToken);
            return PublicClassMessageEnum.SUCCES.getMessageMap();
        } catch (Exception e) {
            if(e instanceof PublicClassException) {
                return (((PublicClassException) e).publicClassMessage).getMessageMap();
            }
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        }
    }


    /**
     * 学生退出公开课课堂
     * @param smallClassId
     * @param studentId
     */
    public void quitPublicClassRoom(Long smallClassId, Long studentId) {
        publicClassRoom.quit(smallClassId, studentId);
    }


    public long getPublicClassRoomStudentCount(Long smallClassId) {
        return publicClassRoom.getClassRoomStudentCount(smallClassId);
    }

    public Set<Long> getPublicClassRoomMembers(Long smallClassId) {
        return publicClassRoom.getPublicClassRoomMembers(smallClassId);
    }

    public Long rollCall(Long smallClassId) {
        return publicClassRoom.rollCall(smallClassId);
    }

    public void evictSmallClassIdById(Long smallClassId) {
        publicClassCacheWithId.evict(smallClassId);
    }

    private SmallClass getClassRoomById(Long smallClassId) {
        return smallClassJpaRepository.findOne(smallClassId);

//        SmallClass smallClass = publicClassCacheWithId.get(smallClassId, SmallClass.class);
//        if(smallClass == null) {
//            synchronized (this) {
//                smallClass = smallClassJpaRepository.findOne(smallClassId);
//                if (smallClass == null) {
//                    throw new PublicClassException(PublicClassMessageEnum.ERROR_PUBLIC_CLASS);
//                }
//                publicClassCacheWithId.putIfAbsent(smallClassId, smallClass);
//            }
//        }
//        return smallClass;
    }

    /**
     * 从数据库中查询课堂
     * @param level
     * @param now
     * @return
     */
    private Map<String, Object> getClassRoomByLevelAndNowWithDatabase(String level, LocalDate now) {
        // 先判断是否是可用的等级
        CourseDifficultyEnum courseDifficulty;
        try {
            courseDifficulty = CourseDifficultyEnum.valueOf(level);
        } catch (Exception e) {
            throw new BusinessException("错误的level等级");
        }
        PublicClassTimeEnum publicClass = PublicClassTimeEnum.publicClassTime(courseDifficulty);
        List<SmallClass> publicClassList = smallClassJpaRepository.findByClassDateAndSlotIdAndClassType(
                DateUtil.convertToDate(now), publicClass.getTimeRange().getSlotId(), ClassTypeEnum.PUBLIC.name());
        if(CollectionUtils.isEmpty(publicClassList)) {
            throw new BusinessException("今天没有对应的公开课!");
        }
        SmallClass smallClass = publicClassList.get(0);
        HashMap<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("classRoom", smallClass);
        resultMap.put("timeRange", publicClass.getTimeRange().setClassDate(now));
        return resultMap;
    }
}
