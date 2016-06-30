package com.boxfishedu.workorder.entity.mysql;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by zijun.jiao on 16/6/28.
 * 用户登陆信息表
 */
@Component
@Data
@Entity
@Table(name = "work_order_user")
public class WorkOrderUser {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "usercode", nullable = false)
    private String userCode;

    @Column(name = "username", nullable = true)
    private String userName;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "createtime", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /** 0  无效  1 有效 **/
    @Column(name = "flag", nullable = false)
    private String flag;




    @Override
    public String toString() {
        return "WorkOrder{" +
                "id=" + id +
                ", usercode=" + userCode +
                ", username=" + userName +
                ", password='" + password + '\'' +
                ", flag=" + flag +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
