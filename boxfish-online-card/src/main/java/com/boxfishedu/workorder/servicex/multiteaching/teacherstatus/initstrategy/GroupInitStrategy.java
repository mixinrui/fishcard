package com.boxfishedu.workorder.servicex.multiteaching.teacherstatus.initstrategy;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.boxfishedu.workorder.web.view.teacher.TeacherView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.transaction.Transactional;
import java.util.*;

/**
 * Created by hucl on 17/1/8.
 */
public interface GroupInitStrategy {
    RecommandCourseView getRecommandCourse(SmallClass smallClass);

    TeacherView getRecommandTeacher(SmallClass smallClass);

    void initGroupClass(SmallClass smallClass);

    default List<Long> workOrders2Students(List<WorkOrder> workOrders) {
        List<Long> studentds = Lists.newArrayList();
        workOrders.forEach(workOrder -> studentds.add(workOrder.getStudentId()));
        return studentds;
    }

    default TutorTypeEnum teachingType2TutorType(SmallClass smallClass) {
        return ((TeachingType) TeachingType.get(smallClass.getRoleId()))
                .teachingType2TutorType();
    }

    default void writeTeacherInfoBack(SmallClass smallClass, List<WorkOrder> workOrders, TeacherView teacherView) {
        if (!Objects.isNull(smallClass.getTeacherId())) {
            workOrders.forEach(workOrder -> {
                workOrder.setSmallClassId(smallClass.getId());
                workOrder.setUpdateTime(new Date());
                workOrder.setAssignTeacherTime(new Date());
                workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
                workOrder.setTeacherId(teacherView.getTeacherId());
                workOrder.setTeacherName(teacherView.getTeacherName());
            });
        }
    }

    default void writeChatRoomBack(SmallClass smallClass, List<WorkOrder> workOrders, FishCardGroupsInfo fishCardGroupsInfo) {
        smallClass.setChatRoomId(fishCardGroupsInfo.getChatRoomId());
        smallClass.setGroupId(fishCardGroupsInfo.getGroupId());
        workOrders.forEach(workOrder -> {
            workOrder.setGroupId(smallClass.getGroupId());
            workOrder.setChatRoomId(smallClass.getChatRoomId());
        });
        smallClass.setAllCards(workOrders);
    }

    default void writeCourseBack(
            SmallClass smallClass, List<WorkOrder> workOrders
            , RecommandCourseView recommandCourseView, RecommandCourseRequester recommandCourseRequester) {
        smallClass.setCourseId(recommandCourseView.getCourseId());
        smallClass.setCourseName(recommandCourseView.getCourseName());
        smallClass.setCourseType(recommandCourseView.getCourseType());
        smallClass.setCover(recommandCourseRequester.getThumbNailPath(recommandCourseView.getCover()));

        if (Objects.isNull(smallClass.getCourseId())) {
            throw new BusinessException("没有获取到课程信息,不作回写");
        }
        workOrders.forEach(workOrder -> {
            workOrder.setCourseId(smallClass.getCourseId());
            workOrder.setCourseName(smallClass.getCourseName());
            workOrder.setCourseType(smallClass.getCourseType());
            if (workOrder.getStatus() != FishCardStatusEnum.TEACHER_ASSIGNED.getCode()) {
                workOrder.setStatus(FishCardStatusEnum.COURSE_ASSIGNED.getCode());
            }
        });
    }

    default void persistSmallClass(SmallClass smallClass, SmallClassJpaRepository smallClassJpaRepository) {
        smallClassJpaRepository.save(smallClass);
    }

    default void persistCardRelatedInfo(
            SmallClass smallClass, WorkOrderService workOrderService
            , ScheduleCourseInfoService scheduleCourseInfoService, RecommandCourseView recommandCourseView) {

        smallClass.getAllCards().forEach(workOrder -> {
            workOrder.setSmallClassId(smallClass.getId());
            workOrderService.save(workOrder);
            List<CourseSchedule> courseSchedules = workOrderService
                    .batchUpdateCourseSchedule(workOrder.getService(), Arrays.asList(workOrder));

            Map<Integer, RecommandCourseView> recommandCourseViewMap = Maps.newHashMap();
            recommandCourseViewMap.put(workOrder.getSeqNum(), recommandCourseView);

            scheduleCourseInfoService.saveSingleCourseInfo(
                    workOrder, courseSchedules.get(0), recommandCourseView);
        });
    }

    FishCardGroupsInfo buildChatRoom(SmallClass smallClass);

    @Transactional
    void persistGroupClass(SmallClass smallClass, List<WorkOrder> workOrders, RecommandCourseView recommandCourseView);
}
