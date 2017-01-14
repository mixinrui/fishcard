package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.Date;

/**
 * Created by hucl on 17/1/14.
 */
@Data
@Entity(noClassnameStored = true)
public class SmallClassStudentsRelation {
    @JsonIgnore
    @Id
    private ObjectId id;

    private Long smallClassId;

    private Long master;

    private Long partner;

    private Date createTime;
}
