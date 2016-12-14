package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.requester.TeacherPhotoRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseType2TeachingTypeService;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import com.boxfishedu.workorder.web.result.InstantClassResult;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

/**
 * Created by hucl on 16/11/3.
 */
@Component
public class InstantClassUpdatorService {
    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional
    public InstantClassCard incrementrequestTeacherTimes(Long id){
        InstantClassCard instantClassCard=instantClassJpaRepository.findForUpdate(id);
        instantClassCard.setRequestMatchTeacherTime(new Date());
        instantClassCard.setRequestTeacherTimes(instantClassCard.getRequestTeacherTimes()+1);
        return  instantClassJpaRepository.save(instantClassCard);
    }

    @Transactional
    public InstantClassCard resetInstantCard(Long id,Integer resultReadFlag,InstantClassRequestStatus instantClassRequestStatus){
        InstantClassCard instantClassCard=instantClassJpaRepository.findForUpdate(id);
        instantClassCard.setRequestMatchTeacherTime(new Date());
        instantClassCard.setRequestTeacherTimes(instantClassCard.getRequestTeacherTimes()+1);
        instantClassCard.setResultReadFlag(resultReadFlag);
        instantClassCard.setStatus(instantClassRequestStatus.getCode());
        return  instantClassJpaRepository.save(instantClassCard);
    }

;

}
