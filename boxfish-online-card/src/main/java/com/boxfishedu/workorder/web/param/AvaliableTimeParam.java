package com.boxfishedu.workorder.web.param;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by hucl on 16/5/12.
 */
@Data
public class AvaliableTimeParam implements Serializable {
    private Long studentId;
    private String comboType;
    private String tutorType;
    private Integer productType;
    //是否免费
    private Boolean isFree;
    private Long orderId;
    private String date;

    /**
     * 默认为0 模板方式
     * 选时间方式, 0 模板 1 自定义方式
     */
    private Integer selectMode;

    //给换课时候判断换课的可用时间使用
    private Long workOrderId;

    // 推迟周数
    private Integer delayWeek;
    // 推迟时间开始点
    private String    rangeStartTime;

    public ComboTypeToRoleId getComboTypeEnum() {
        return ComboTypeToRoleId.resolve(comboType);
    }
}
