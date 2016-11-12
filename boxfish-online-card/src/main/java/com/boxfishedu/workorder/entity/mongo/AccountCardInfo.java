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
 * Created by hucl on 16/9/24.
 * 用户首页信息
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(noClassnameStored = true)
public class AccountCardInfo {
    @JsonIgnore
    @Id
    private ObjectId id;

    @JsonIgnore
    @Indexed(background = true, unique = true)
    private Long studentId;

    @JsonIgnore
    private Date createTime;

    @JsonIgnore
    private Date updateTime;

    private AccountCourseBean chinese;
    private AccountCourseBean foreign;
    private AccountCourseBean comment;

    public static AccountCardInfo buildEmpty(){
        AccountCourseBean emptyBean=new AccountCourseBean();
        emptyBean.setLeftAmount(0);
        emptyBean.setCourseInfo(null);
        AccountCardInfo accountCardInfo=new AccountCardInfo();
        accountCardInfo.setChinese(emptyBean);
        accountCardInfo.setForeign(emptyBean);
        return accountCardInfo;
    }
}


