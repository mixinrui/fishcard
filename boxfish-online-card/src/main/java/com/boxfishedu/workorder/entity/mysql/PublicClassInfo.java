package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.dao.jpa.LocalDateAttributeConverter;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by LuoLiBing on 17/1/9.
 * 公开课详情
 */
@Data
@Entity
@Table(name = "public_class_info")
public class PublicClassInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long smallClassId;

    private Long studentId;

    private String studentName;

    @Convert(converter = LocalDateAttributeConverter.class)
    private LocalDate classDate;

    private Integer slotId;

    private Date startTime;

    private Date createTime;

    private Date startTime;

    private String studentName;

    private Integer status;

    public PublicClassInfo() {
        this.createTime = new Date();
        this.status = PublicClassInfoStatusEnum.ENTER.getCode();
    }
}
