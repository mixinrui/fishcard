package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

/**
 * Created by hucl on 16/11/4.
 */
@Data
@Entity(noClassnameStored = true)
public class InstantClassTimeRules {
    @Id
    @JsonIgnore
    private ObjectId id;

    //日期
    @Indexed(background = true)
    private String date;

    private String day;

    //开始时间
    private String begin;

    //结束时间
    private String end;

    //CN,FRN
    private String tutorType;

}
