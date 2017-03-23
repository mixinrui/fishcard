package com.boxfishedu.workorder.web.param;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/4/26.
 */
@Data
public class SmallClassSuperStuParam {
    private Long  id; //
    private String classType;
    private Long studentId;
    private Long workOrderId;
    private String message;
}
