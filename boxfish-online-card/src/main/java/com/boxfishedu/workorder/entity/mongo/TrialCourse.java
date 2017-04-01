package com.boxfishedu.workorder.entity.mongo;

import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.util.Objects;

/**
 * Created by hucl on 16/6/27.
 */
@Data
@Entity(noClassnameStored = true)
public class TrialCourse {
    @Id
    private ObjectId id;

    @Indexed(background = true)
    private String courseId;
    private String name;
    private String courseType;
    private String publicDate;
    private String difficulty;
    private String thumbnail;
    private Long lastModified;
    //课表的类型:"TRIAL"试讲 "NORMAL"正常
    private String scheduleType;

    public TrialCourse() {

    }

    public TrialCourse(RecommandCourseView recommandCourseView, String thumbnailServer) {
        this.courseId = recommandCourseView.getCourseId();
        if (!StringUtils.isEmpty(recommandCourseView.getEnglishName())) {
            this.name = recommandCourseView.getEnglishName();
        } else {
            this.name = recommandCourseView.getCourseName();
        }
        this.courseType = recommandCourseView.getCourseType();
        this.difficulty = recommandCourseView.getDifficulty();
        this.thumbnail = String.format("%s%s", thumbnailServer, recommandCourseView.getCover());
        this.scheduleType = "TRIAL";
    }

    @Override
    public String toString() {
        return this.id + "#" + this.courseId;
    }
}
