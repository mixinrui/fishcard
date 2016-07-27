package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Date;

/**
 * Created by hucl on 16/4/11.
 */
@Data
@Entity(noClassnameStored = true)
public class WorkOrderLog {
    @JsonIgnore
    @Id
    private ObjectId id;

    @Transient
    private Integer seqNum;

    private Integer status;

    @JsonProperty("statusDesc")
    private String content;

    private Date createTime;

    //主要是给删除记录用的
    private String deleteFlag;

    //主要是能够再找回以前的数据能进行恢复
    private String workOrderJson;

    private String courseScheduleJson;

    @Indexed(background = true)
    private Long workOrderId;

    @Override
    public String toString(){
        return this.id + "#" + this.status + "#" + this.content + "#" + this.createTime + "#" + this.workOrderId;
    }
}
