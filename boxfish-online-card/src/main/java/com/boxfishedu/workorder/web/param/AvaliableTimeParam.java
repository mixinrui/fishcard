package com.boxfishedu.workorder.web.param;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by hucl on 16/5/12.
 */
@Data
public class AvaliableTimeParam implements Serializable {
    private Long studentId;
    private String comboType;
    //是否免费
    private Boolean isFree;
    private Long orderId;
    private String date;

    //给换课时候判断换课的可用时间使用
    private Long workOrderId;
    public ComboTypeToRoleId getComboType() {
        return ComboTypeToRoleId.resolve(comboType);
    }
}
