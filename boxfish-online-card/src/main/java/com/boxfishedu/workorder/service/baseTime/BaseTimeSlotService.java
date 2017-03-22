package com.boxfishedu.workorder.service.baseTime;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.BaseTimeSlotJpaRepository;
import com.boxfishedu.workorder.entity.mysql.BaseTimeSlots;
import com.boxfishedu.workorder.service.RedisMapService;
import com.boxfishedu.workorder.web.param.BaseTimeSlotParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/11/23.
 */
@Service
public class BaseTimeSlotService {

    @Autowired
    private BaseTimeSlotJpaRepository baseTimeSlotJpaRepository;

    @Autowired
    private RedisMapService redisMapService;

    // 1对1 添加时间片

    public void addTimeSlots(Integer slots) {
        Date dateTo = baseTimeSlotJpaRepository.findMaxDate();
        if (dateTo == null) {
            return;
        }

        LocalDate localDateTo = Instant.ofEpochMilli(dateTo.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate localDate = LocalDate.now();

        int days = (int) (localDateTo.toEpochDay() - localDate.toEpochDay());

        for (int i = 1; i <= days; i++) {

            LocalDate localt = localDate.plusDays(i);

            //中教
            List<BaseTimeSlots> list = findByDate(localt, 0, slots);
            //if(localt.getDayOfWeek().getValue()<= 5) {
            try {
                baseTimeSlotJpaRepository.save(list);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //外教
            try {
                list = findByDate(localt, 1, slots);
                baseTimeSlotJpaRepository.save(list);
            } catch (Exception e) {
                e.printStackTrace();
            }


            //}
        }

    }


    private List<BaseTimeSlots> findByDate(LocalDate localDate, int teachingType, Integer slot) {
        List<BaseTimeSlots> result = new ArrayList<>();
        int from = slot, to = slot;

        for (int i = from; i <= to; i++) {
            result.add(createBaseTimeSlots(i, localDate, teachingType));
        }
        return result;
    }


    @Transactional
    public void initBaseTimeSlots(int days) {
        Date date = baseTimeSlotJpaRepository.findMaxDate();
        if (date == null) {
            date = new Date();
        }
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        for (int i = 1; i <= days; i++) {
            try {
                List<BaseTimeSlots> list = findByDate(localDate.plusDays(i), 0);
                baseTimeSlotJpaRepository.save(list);

                list = findByDate(localDate.plusDays(i), 1);
                baseTimeSlotJpaRepository.save(list);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private List<BaseTimeSlots> findByDate(LocalDate localDate, int teachingType) {
        List<BaseTimeSlots> result = new ArrayList<>();
        int from, to;
        if (localDate.getDayOfWeek().getValue() > 5) {
            from = 1;
            to = 34;
        } else {
            from = 23;
            to = 34;
        }
        for (int i = from; i <= to; i++) {
            result.add(createBaseTimeSlots(i, localDate, teachingType));
        }
        return result;
    }


    private BaseTimeSlots createBaseTimeSlots(int slotId, LocalDate localDate, int teachingType) {
        BaseTimeSlots t1 = new BaseTimeSlots();
        t1.setClassDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        t1.setSlotId(slotId);
        t1.initTime();

        if (teachingType == 0 && (slotId == 23 || slotId == 24)) {
            t1.setProbability(0);
        } else {

            t1.setProbability(100);
        }
        t1.setTeachingType(teachingType);
        t1.setClientType(0);
        return t1;
    }


    public Page<BaseTimeSlots> findByTeachingTypeAndClassDateBetween(BaseTimeSlotParam baseTimeSlotParam, Pageable pageable) {
        validate(baseTimeSlotParam);
        processDateParam(baseTimeSlotParam);
        return baseTimeSlotJpaRepository.findByTeachingTypeAndClassDateBetween(baseTimeSlotParam.getTeachingType(), baseTimeSlotParam.getBeginDateFormat(), baseTimeSlotParam.getEndDateFormat(), pageable);
    }

    public List<BaseTimeSlots> findByTeachingTypeAndClassDateBetween(BaseTimeSlotParam baseTimeSlotParam) {
        validate(baseTimeSlotParam);
        processDateParam(baseTimeSlotParam);
        return baseTimeSlotJpaRepository.findByTeachingTypeAndClassDateBetween(baseTimeSlotParam.getTeachingType(), baseTimeSlotParam.getBeginDateFormat(), baseTimeSlotParam.getEndDateFormat());
    }

    @Transactional
    public void modify(List<BaseTimeSlots> baseTimeSlotsList) {
        if (CollectionUtils.isEmpty(baseTimeSlotsList)) {
            return;
        }
        Map<String, List<BaseTimeSlots>> rediscache = Maps.newHashMap();
        for (BaseTimeSlots baseTimeSlots : baseTimeSlotsList) {
            String key = baseTimeSlots.getTeachingType() + "" + BaseTimeSlots.CLIENT_TYPE_STU + DateUtil.date2SimpleString(baseTimeSlots.getClassDate());
            if (null != rediscache.get(key)) {
                List temp = rediscache.get(key);
                temp.add(baseTimeSlots);
                rediscache.put(key, temp);
            } else {
                rediscache.put(key, Lists.newArrayList(baseTimeSlots));
            }
        }


        for (String key : rediscache.keySet()) {
            BaseTimeSlots baseTimeSlots = rediscache.get(key).get(0);
            if (null != baseTimeSlots) {
                redisMapService.delMap(baseTimeSlots.getTeachingType() + "" + BaseTimeSlots.CLIENT_TYPE_STU, DateUtil.date2SimpleString(baseTimeSlots.getClassDate()));
            }
        }

        baseTimeSlotJpaRepository.save(baseTimeSlotsList);
    }

    private void validate(BaseTimeSlotParam baseTimeSlotParam) {
        if (null == baseTimeSlotParam || baseTimeSlotParam.getTeachingType() == null || null == baseTimeSlotParam.getBeginDate() || null == baseTimeSlotParam.getEndDate()) {
            throw new BusinessException("参数有误,请重新输入");
        }
    }


    public void processDateParam(BaseTimeSlotParam baseTimeSlotParam) {
        if (null != baseTimeSlotParam.getBeginDate()) {
            baseTimeSlotParam.setBeginDateFormat(DateUtil.String2Date(baseTimeSlotParam.getBeginDate() + " 00:00:00"));
        }

        if (null != baseTimeSlotParam.getEndDate()) {
            baseTimeSlotParam.setEndDateFormat(DateUtil.String2Date(baseTimeSlotParam.getEndDate() + " 23:59:59"));
        }
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.String2Date("2012-12-12 00:00:00"));
        System.out.println(LocalDate.now().getDayOfWeek().getValue());
    }
}