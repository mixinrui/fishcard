package com.boxfishedu.workorder.service.baseTime;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.BaseTimeSlotJpaRepository;
import com.boxfishedu.workorder.entity.mysql.BaseTimeSlots;
import com.boxfishedu.workorder.web.param.BaseTimeSlotParam;
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

/**
 * Created by LuoLiBing on 16/11/23.
 */
@Service
public class BaseTimeSlotService {

    @Autowired
    private BaseTimeSlotJpaRepository baseTimeSlotJpaRepository;

    public void initBaseTimeSlots(int days) {
        Date date = baseTimeSlotJpaRepository.findMaxDate();
        if(date == null) {
            date = new Date();
        }
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        for(int i = 1; i <= days; i++) {
            List<BaseTimeSlots> list = findByDate(localDate.plusDays(i), 0);
            baseTimeSlotJpaRepository.save(list);

            list = findByDate(localDate.plusDays(i), 1);
            baseTimeSlotJpaRepository.save(list);
        }
    }

    private List<BaseTimeSlots> findByDate(LocalDate localDate, int teachingType) {
        List<BaseTimeSlots> result = new ArrayList<>();
        int from, to;
        if(localDate.getDayOfWeek().getValue() > 5) {
            from = 1; to = 34;
        } else {
            from = 25; to = 34;
        }
        for(int i = from; i <=to; i++) {
            result.add(createBaseTimeSlots(i, localDate, teachingType));
        }
        return result;
    }


    private BaseTimeSlots createBaseTimeSlots(int slotId, LocalDate localDate, int teachingType) {
        BaseTimeSlots t1 = new BaseTimeSlots();
        t1.setClassDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        t1.setSlotId(slotId);
        t1.initTime();
        t1.setProbability(100);
        t1.setTeachingType(teachingType);
        t1.setClientType(0);
        return t1;
    }


    public Page<BaseTimeSlots> findByTeachingTypeAndClassDateBetween(BaseTimeSlotParam baseTimeSlotParam, Pageable pageable){
        validate(baseTimeSlotParam);
        processDateParam(baseTimeSlotParam);
        return  baseTimeSlotJpaRepository.findByTeachingTypeAndClassDateBetween(baseTimeSlotParam.getTeachingType(), baseTimeSlotParam.getBeginDateFormat(),baseTimeSlotParam.getEndDateFormat(), pageable);
    }

    public List<BaseTimeSlots>  findByTeachingTypeAndClassDateBetween(BaseTimeSlotParam baseTimeSlotParam){
        validate(baseTimeSlotParam);
        processDateParam(baseTimeSlotParam);
        return  baseTimeSlotJpaRepository.findByTeachingTypeAndClassDateBetween(baseTimeSlotParam.getTeachingType(),  baseTimeSlotParam.getBeginDateFormat(),baseTimeSlotParam.getEndDateFormat());
    }

    @Transactional
    public void modify(List<BaseTimeSlots> baseTimeSlotsList){
        if(CollectionUtils.isEmpty(baseTimeSlotsList)){
            return;
        }
        baseTimeSlotJpaRepository.save(baseTimeSlotsList);
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