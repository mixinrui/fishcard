package com.boxfishedu.workorder.service;


import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;

import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;

import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecordsResult;
import com.boxfishedu.workorder.service.base.BaseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import com.google.common.base.Optional;


/**
 * Created by hucl on 16/3/31.
 */
@Component
public class StStudentApplyRecordsService extends BaseService<StStudentApplyRecords, StStudentApplyRecordsJpaRepository, Long> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 获取已经指定过老师的记录 AssignTeacherApplyStatusEnum
    public StStudentApplyRecords  getStStudentApplyRecordsBy(Long workOrderId,StStudentApplyRecords.ApplyStatus applyStatus){
        return jpa.findTop1ByWorkOrderIdAndApplyStatusAndValid(workOrderId,applyStatus,StStudentApplyRecords.VALID.yes);
    }


    public Integer getUnreadInvitedNum(Long teacherId, Date date){
       Optional<Long> unread = jpa.getUnreadInvitedNum(teacherId,date, StStudentApplyRecords.ReadStatus.no,StStudentApplyRecords.VALID.yes);
        return unread.isPresent()?unread.get().intValue():0;
    }


    public Page<StStudentApplyRecordsResult> getmyInviteList(Long teacherId, Date date, Pageable pageable){
        return  jpa.getmyInviteList(teacherId,date,StStudentApplyRecords.VALID.yes, pageable);
    }

    public Page<StStudentApplyRecords> getMyClassesByStudentId(Long teacherId,Long studentId,Date date,Pageable pageable){
        return  jpa.findByApplyTimeGreaterThanAndTeacherIdAndStudentIdAndValid(date,teacherId,studentId,StStudentApplyRecords.VALID.yes,pageable);
    }

    public List<StStudentApplyRecords> getMyClassesByStudentId(Long teacherId,Long studentId,Date date){
        return  jpa.findByApplyTimeGreaterThanAndTeacherIdAndStudentIdAndValid(date,teacherId,studentId,StStudentApplyRecords.VALID.yes);
    }

    public StStudentApplyRecords findMyLastAssignTeacher(Long studentId,Integer skuId){
        return jpa.findTop1ByStudentIdAndApplyStatusAndSkuIdAndValidAndTeacherIdNotNullOrderByApplyTimeDesc(studentId,StStudentApplyRecords.ApplyStatus.agree,skuId,StStudentApplyRecords.VALID.yes);
    }

    public int upateReadStatusByStudentId(Long teacherId, Long studentId){
        return  jpa.setFixedIsReadFor(StStudentApplyRecords.ReadStatus.yes,teacherId,studentId,StStudentApplyRecords.VALID.yes);
    }

}
