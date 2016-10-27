package com.boxfishedu.workorder.entity.mongo;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.util.Date;

/**
 * Created by hucl on 16/10/25.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(noClassnameStored = true)
public class OnlineAccountSet {
    @JsonIgnore
    @Id
    private ObjectId id;

    @JsonIgnore
    @Indexed(background = true, unique = true)
    private Long studentId;

    @JsonIgnore
    private Date updateTime;
}
