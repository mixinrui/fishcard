package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 16/11/17.
 */
@Data
@Entity(noClassnameStored = true)
public class InstantCardLog {
    @JsonIgnore
    @Id
    private ObjectId id;

    private Integer status;

    private Long studentId;

    private String desc;

    private Date createTime;

    @Indexed(background = true)
    private Long instantCardId;

    private Long workOrderId;

    private Date requestTeacherTime;

    private List<Long>  pullTeacherIds;

    @Override
    public String toString(){
        return this.id + "#" + this.status + "#" + this.desc + "#" + this.createTime ;
    }
}
