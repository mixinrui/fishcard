package com.boxfishedu.workorder.service.baseTime;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.BaseTimeSlotJpaRepository;
import com.boxfishedu.workorder.dao.jpa.BaseTimeSlotJpaSmallClassRepository;
import com.boxfishedu.workorder.entity.mysql.BaseTimeSlots;
import com.boxfishedu.workorder.entity.mysql.BaseTimeSlotsSmallClass;
import com.boxfishedu.workorder.service.RedisMapService;
import com.boxfishedu.workorder.web.param.BaseTimeSlotParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by LuoLiBing on 16/11/23.
 */
@Service
public class BaseTimeSlotSmallClassService {

    @Autowired
    private BaseTimeSlotJpaSmallClassRepository baseTimeSlotJpaSmallClassRepository;

    @Autowired
    private RedisMapService redisMapService;


    //初始化小班课时间片
    public void initBaseTimeSlotsSmallClass(int days) {
        Date date = baseTimeSlotJpaSmallClassRepository.findMaxDate();
        if(date == null) {
            date = new Date();
        }
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        for(int i = 1; i <= days; i++) {
            List<BaseTimeSlotsSmallClass> list = findByDate(localDate.plusDays(i), 0);
            baseTimeSlotJpaSmallClassRepository.save(list);

            list = findByDate(localDate.plusDays(i), 1);
            baseTimeSlotJpaSmallClassRepository.save(list);
        }
    }

    private List<BaseTimeSlotsSmallClass> findByDate(LocalDate localDate, int teachingType) {
        List<BaseTimeSlotsSmallClass> result = new ArrayList<>();
        int from, to;
        if(localDate.getDayOfWeek().getValue() > 5) {
            from = 1; to = 34;
        } else {
            from = 23; to = 34;
        }
        for(int i = from; i <=to; i++) {
            result.add(createBaseTimeSlots(i, localDate, teachingType));
        }
        return result;
    }


    private BaseTimeSlotsSmallClass createBaseTimeSlots(int slotId, LocalDate localDate, int teachingType) {
        BaseTimeSlotsSmallClass t1 = new BaseTimeSlotsSmallClass();
        t1.setClassDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        t1.setSlotId(slotId);
        t1.initTime();

        // 目前 初始化 小班课 20:00点 和 20:30 两个时间片为100
        if( slotId==27 || slotId==28){
            t1.setProbability(100);
        }else {
            t1.setProbability(0);
        }

        t1.setTeachingType(teachingType);
        t1.setClientType(0);
        return t1;
    }


    public Page<BaseTimeSlotsSmallClass> findByTeachingTypeAndClassDateBetween(BaseTimeSlotParam baseTimeSlotParam, Pageable pageable){
        validate(baseTimeSlotParam);
        processDateParam(baseTimeSlotParam);
        return  baseTimeSlotJpaSmallClassRepository.findByTeachingTypeAndClassDateBetween(baseTimeSlotParam.getTeachingType(), baseTimeSlotParam.getBeginDateFormat(),baseTimeSlotParam.getEndDateFormat(), pageable);
    }

    public List<BaseTimeSlotsSmallClass>  findByTeachingTypeAndClassDateBetween(BaseTimeSlotParam baseTimeSlotParam){
        validate(baseTimeSlotParam);
        processDateParam(baseTimeSlotParam);
        return  baseTimeSlotJpaSmallClassRepository.findByTeachingTypeAndClassDateBetween(baseTimeSlotParam.getTeachingType(),  baseTimeSlotParam.getBeginDateFormat(),baseTimeSlotParam.getEndDateFormat());
    }

    @Transactional
    public void modify(List<BaseTimeSlotsSmallClass> baseTimeSlotsList){
        if(CollectionUtils.isEmpty(baseTimeSlotsList)){
            return;
        }
        Map<String ,List<BaseTimeSlotsSmallClass>> rediscache = Maps.newHashMap();
        for(BaseTimeSlotsSmallClass baseTimeSlots:baseTimeSlotsList){
            String key = baseTimeSlots.getTeachingType()+""+BaseTimeSlots.CLIENT_TYPE_STU+  DateUtil.date2SimpleString(baseTimeSlots.getClassDate());
            if(null!=rediscache.get(key)){
                List temp = rediscache.get(key);
                temp.add(baseTimeSlots);
                rediscache.put(key,  temp   );
            }else {
                rediscache.put(key, Lists.newArrayList(baseTimeSlots));
            }
        }


        for(String key:rediscache.keySet()){
            BaseTimeSlotsSmallClass baseTimeSlots = rediscache.get(key).get(0);
            if(null != baseTimeSlots){
                redisMapService.delMapSmallClass(baseTimeSlots.getTeachingType() +""+BaseTimeSlots.CLIENT_TYPE_STU ,DateUtil.date2SimpleString(baseTimeSlots.getClassDate()) );
            }
        }

        baseTimeSlotJpaSmallClassRepository.save(baseTimeSlotsList);
    }

    private void  validate(BaseTimeSlotParam baseTimeSlotParam){
        if(null==baseTimeSlotParam || baseTimeSlotParam.getTeachingType()==null   || null ==  baseTimeSlotParam.getBeginDate() || null == baseTimeSlotParam.getEndDate()){
            throw new BusinessException("参数有误,请重新输入");
        }
    }


    public void processDateParam(BaseTimeSlotParam baseTimeSlotParam) {
        if (null != baseTimeSlotParam.getBeginDate()) {
            baseTimeSlotParam.setBeginDateFormat(DateUtil.String2Date(baseTimeSlotParam.getBeginDate()+" 00:00:00"));
        }

        if (null != baseTimeSlotParam.getEndDate()) {
            baseTimeSlotParam.setEndDateFormat(DateUtil.String2Date(baseTimeSlotParam.getEndDate()+" 23:59:59"));
        }
    }
    public static void main(String[] args) {
        System.out.println(DateUtil.String2Date("2012-12-12 00:00:00"));
        System.out.println(LocalDate.now().getDayOfWeek().getValue());
    }
}