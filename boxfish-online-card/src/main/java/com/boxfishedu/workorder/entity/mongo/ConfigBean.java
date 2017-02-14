package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by hucl on 17/2/7.
 * 配置表
 */
@Data
@Entity(noClassnameStored = true)
public class ConfigBean {
    @JsonIgnore
    @Id
    private ObjectId id;

    //公开课上线提醒:外教大讲堂预计2月15日开放，欢迎使用
    private String publicWarning;

    private String coverTitle;

    private String coverDesc;

}
