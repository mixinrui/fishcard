package com.boxfishedu.workorder.web.param.fishcardcenetr;

import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/7/26.
 */
@Data
public class SmallClassAddStuParam {
    //批量增加学生id
    private List<Long> studentIds;

    //小班课id
    private  Long id;

    //中外教 1 中教 2 外教
    private Integer roleId;

    // FSCC 中教 FSCF 外教
    private String comboType;




}
