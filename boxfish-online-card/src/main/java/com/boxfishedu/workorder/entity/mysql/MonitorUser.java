package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * Created by ansel on 2017/3/20.
 */
@Component
@Data
@Table(name = "monitor_user")
@Entity
public class MonitorUser {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "pass_word")
    private String passWord;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "user_type")
    private String userType;

    @Column(name = "avg_sum")
    private Integer avgSum;

    @Column(name = "enabled")
    private Integer enabled;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "create_time")
    private Date createTime;

    public MonitorUser(){
        this.updateTime = new Date();
        this.createTime = new Date();
    }

    public MonitorUser(MonitorUserRequestForm monitorUserRequestForm){
        if (Objects.nonNull(monitorUserRequestForm.getUserId())){
            this.userId = monitorUserRequestForm.getUserId();
        }
        if (Objects.nonNull(monitorUserRequestForm.getUserName())){
            this.userName = monitorUserRequestForm.getUserName();
        }
        if (Objects.nonNull(monitorUserRequestForm.getPassWord())){
            this.passWord = monitorUserRequestForm.getPassWord();
        }
        if (Objects.nonNull(monitorUserRequestForm.getAccessToken())){
            this.accessToken = monitorUserRequestForm.getAccessToken();
        }
        if (Objects.nonNull(monitorUserRequestForm.getUserType())){
            this.userType = monitorUserRequestForm.getUserType();
        }
        this.enabled = 1;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}
