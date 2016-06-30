package com.boxfishedu.workorder.entity.mongo;

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

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
    private Integer difficulty;
    private String thumbnail;
    private Long lastModified;
    //课表的类型:"TRIAL"试讲 "NORMAL"正常
    private String scheduleType;

    @Override
    public String toString(){
        return this.id + "#" + this.courseId;
    }
}
