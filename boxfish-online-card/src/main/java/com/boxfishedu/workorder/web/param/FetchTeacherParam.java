package com.boxfishedu.workorder.web.param;

import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/4/21.
 */
@Data
public class FetchTeacherParam implements Serializable {
    private Long userId;
    private List<ScheduleModel> scheduleModelList;

    public FetchTeacherParam() {
        scheduleModelList = Lists.newArrayList();
    }

    public void addScheduleModel(ScheduleModel scheduleModel) {
        if (scheduleModel != null) {
            scheduleModelList.add(scheduleModel);
        }
    }

    public ScheduleBatchReq convertToScheduleBatchReq() {
        ScheduleBatchReq scheduleBatchReq = new ScheduleBatchReq();
        scheduleBatchReq.setUserId(this.userId);
        List<com.boxfishedu.workorder.web.param.bebase3.ScheduleModel> scheduleModelList = Lists.newArrayList();
        scheduleBatchReq.setScheduleModelList(scheduleModelList);
        if (this.scheduleModelList != null) {
            this.scheduleModelList.stream().forEach(scheduleModel -> {
                com.boxfishedu.workorder.web.param.bebase3.ScheduleModel bean = new com.boxfishedu.workorder.web.param.bebase3.ScheduleModel();
                bean.setId(scheduleModel.getId());
                bean.setCourseType(scheduleModel.getCourseType());
                bean.setDay(new Date(scheduleModel.getDay()));
                bean.setRoleId(scheduleModel.getRoleId());
                bean.setSlotId(scheduleModel.getSlotId());
                scheduleModelList.add(bean);
            });
        }
        return scheduleBatchReq;
    }

    public static List<FetchTeacherParam> fetchTeacherParamList(Map<Long, List<CourseSchedule>> groupCourseScheduleMap) {
        if (groupCourseScheduleMap == null) {
            return null;
        }
        List<FetchTeacherParam> result = Lists.newArrayList();
        groupCourseScheduleMap.forEach((studentId, studentCourseScheduleList) -> {
            FetchTeacherParam fetchTeacherParam = new FetchTeacherParam();
            fetchTeacherParam.setUserId(studentId);
            studentCourseScheduleList.forEach(courseSchedule -> {
                fetchTeacherParam.addScheduleModel(new ScheduleModel(courseSchedule));
            });
            result.add(fetchTeacherParam);
        });
        return result;
    }

}


