package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.dao.jpa.LocalDateAttributeConverter;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by LuoLiBing on 17/1/9.
 * 公开课详情
 */
@Data
@Entity
@Table(name = "public_class_info")
public class PublicClassInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long smallClassId;

    private Long studentId;

    @Convert(converter = LocalDateAttributeConverter.class)
    private LocalDate classDate;

    private Integer slotId;

    private Date createTime;

    public PublicClassInfo() {
        this.createTime = new Date();
    }
}
