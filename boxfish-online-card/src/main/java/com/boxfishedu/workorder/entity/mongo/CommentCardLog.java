package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Transient;

import java.util.Date;

@Data
@Entity(noClassnameStored = true)
public class CommentCardLog {
    @JsonIgnore
    @Id
    private ObjectId id;

    private Integer status;

    @JsonProperty("statusDesc")
    private String content;

    private Date createTime;

    @Indexed(background = true)
    private Long commentCardId;

    @Override
    public String toString(){
        return this.id + "#" + this.status + "#" + this.content + "#" + this.createTime + "#" + this.commentCardId;
    }
}
