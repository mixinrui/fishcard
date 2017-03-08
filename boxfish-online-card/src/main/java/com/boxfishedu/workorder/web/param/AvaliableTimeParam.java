package com.boxfishedu.workorder.web.param;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.servicex.bean.SelectMessageEnum;
import lombok.Data;

import java.io.Serializable;

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
    // 几周完成
    private Integer totalWeeks;
    // 推迟时间开始点
    private String    rangeStartTime;

    private String classType; // classType 为 SMALL  小班课

    public ComboTypeToRoleId getComboTypeEnum() {
        return ComboTypeToRoleId.resolve(comboType);
    }

    public String selectMessage(Integer class_num) {
        // 老版本几周完成不传, 返回默认值
        if(totalWeeks == null) {
            return SelectMessageEnum.DEFAULT.message;
        }
        ComboTypeToRoleId comboType = ComboTypeToRoleId.valueOf(this.comboType);
        ClassTypeEnum classType;
        if(comboType.isSmallClassType()) {
            classType = ClassTypeEnum.SMALL;
        } else {
            classType = ClassTypeEnum.NORMAL;
        }
        SelectMessageEnum.Weeks weeks = SelectMessageEnum.Weeks.resolve(totalWeeks);
        String message = SelectMessageEnum.resolve(classType, weeks,class_num).message;
        return message.replaceAll("%@", Integer.toString(totalWeeks - 1));
    }
}
