package com.boxfishedu.workorder.entity.mongo;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

/**
 * Created by hucl on 16/9/24.
 * 用户首页信息
 */
@Data
@Entity(noClassnameStored = true)
public class AccountCardInfo {
    @JsonIgnore
    @Id
    private ObjectId id;

    @Indexed(background = true)
    private Long studentId;

    private AccountCourseBean chinese;
    private AccountCourseBean foreign;
    private AccountCourseBean comment;
}


