package com.boxfishedu.workorder.servicex.monitor;

import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Created by jiaozijun on 17/4/6.
 */
public class MonitorUserServiceX {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private CourseScheduleRepository courseScheduleRepository;

    /**
     * 超级用户更换课程需要清理之前的鱼卡课程
     * @param studentId
     * @param smallClassId
     * @return
     */
    @Transactional
    public boolean  deleteMoniorCourse(Long studentId,Long smallClassId){
        if(Objects.isNull(studentId) || Objects.isNull(smallClassId)){
            logger.info("@deleteMoniorCourse_params_not_available");
            return false;
        }
        WorkOrder workOrder = workOrderJpaRepository.findBySmallClassIdAndStudentId(studentId,smallClassId);
        if(Objects.isNull(workOrder)){
            logger.info("@deleteMoniorCourse_params_no_class");
            //还没有产生鱼卡 可以进行更换操作
            return true;
        }

        CourseSchedule courseSchedule =  courseScheduleRepository.findByWorkorderId(workOrder.getId());
        if(Objects.isNull(courseSchedule)){
            //课程信息有误不能更换,请先核对数据在进行更换
            logger.error("@deleteMoniorCourse_date_error");
            return false;
        }


        workOrderJpaRepository.delete(workOrder.getId());
        courseScheduleRepository.delete(courseSchedule.getId());

        return true;

    }
}
