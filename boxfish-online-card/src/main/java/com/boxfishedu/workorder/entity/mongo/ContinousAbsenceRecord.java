package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import java.util.Date;

/**
 * Created by hucl on 16/9/17.
 */
@Data
@Entity(noClassnameStored = true)
public class ContinousAbsenceRecord {
    @JsonIgnore
    @Id
    private ObjectId id;

    private Date createTime;

    private Date updateTime;

    @Indexed(background = true)
    private Long studentId;

    private Integer continusAbsenceNum;

    private String comboType;

    @Override
    public String toString(){
        return this.id + "#" + this.studentId + "#" + this.continusAbsenceNum+"#"+this.comboType;
    }
}
