package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by LuoLiBing on 16/11/23.
 *
 */
@Entity
@Table
@Data
public class BaseTimeSlots implements Serializable {

    public final static Integer TEACHING_TYPE_CN = 0;

    public final static Integer TEACHING_TYPE_FRN = 1;

    public final static Integer CLIENT_TYPE_STU = 0;

    public final static Integer CLIENT_TYPE_TEA = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Date classDate;
    private Integer slotId;
    private Date startTime;
    private Date endTime;
    // 概率
    private Integer probability;
    private Integer teachingType;
    private Integer clientType;

    public boolean roll() {
        return ThreadLocalRandom.current().nextInt(100) + 1 <= probability;
    }
}
