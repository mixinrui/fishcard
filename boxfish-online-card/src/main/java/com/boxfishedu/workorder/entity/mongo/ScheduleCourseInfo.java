package com.boxfishedu.workorder.entity.mongo;

import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

/**
 * Created by hucl on 16/6/12.
 */
@Data
@Entity(noClassnameStored = true)
public class ScheduleCourseInfo {
    @Id
    private ObjectId id;

    @Indexed(background = true)
    private String courseId;
    private String name;
    private String englishName;
    private String courseType;
    private String publicDate;
    private String difficulty;
    private String thumbnail;
    private Long lastModified;
    //课表的类型:"TRIAL"试讲 "NORMAL"正常
    private String scheduleType;
    @Indexed(background = true)
    private Long workOrderId;
    @Indexed(background = true)
    private Long scheduleId;

    @Override
    public String toString(){
        return this.id + "#" + this.courseId + "#" +this.workOrderId;
    }

    public static ScheduleCourseInfo create(String thumbnailServer, CourseSchedule schedule,
                                            RecommandCourseView recommandCourseView) {
        ScheduleCourseInfo scheduleCourseInfo=new ScheduleCourseInfo();
        scheduleCourseInfo.setCourseType(schedule.getCourseType());
        String thumbnail = String.format("%s%s", thumbnailServer, recommandCourseView.getCover());
        scheduleCourseInfo.setThumbnail(thumbnail);
        scheduleCourseInfo.setName(recommandCourseView.getCourseName());
        scheduleCourseInfo.setEnglishName(recommandCourseView.getEnglishName());
        scheduleCourseInfo.setDifficulty(recommandCourseView.getDifficulty());
        scheduleCourseInfo.setCourseId(recommandCourseView.getCourseId());
        scheduleCourseInfo.setWorkOrderId(schedule.getWorkorderId());
        scheduleCourseInfo.setScheduleId(schedule.getId());
        return scheduleCourseInfo;
    }
}
