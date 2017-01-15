package com.boxfishedu.workorder.servicex.smallclass.groupbuilder;

import lombok.Data;

import java.util.Date;

/**
 * Created by hucl on 17/1/11.
 */
@Data
public class GroupKey {
    private Date date;
    private Integer slotId;
    private String difficulty;
    private Long studyCounter;

}
