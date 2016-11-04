package com.boxfishedu.workorder.web.param;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/4/26.
 */
@Data
public class InstantRequestParam {
    private Long orderId;
    private Integer productType;
    private String tutorType;
    private String comboType;
    private Long studentId;
    /**
     * 选时间方式, 0 课程表的入口 1 其他入口
     */
    private Integer selectMode;
}
