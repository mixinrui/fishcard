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
public class TimeSlotParam {

    private Integer skuId; // 1 中教   2 外教
    private Long orderId;
    private Integer productType;
    private String tutorType;
    private String comboType;
    @JsonIgnore
    private Long studentId;
    /**
     * 默认为0 模板方式
     * 选时间方式, 0 模板 1 自定义方式
     */
    private Integer selectMode;
    private List<SelectedTime> selectedTimes;

    public ComboTypeToRoleId getComboTypeEnum() {
        return ComboTypeToRoleId.resolve(comboType);
    }

    public static TimeSlotParam instantCard2TimeParam(InstantClassCard instantClassCard){
        TimeSlotParam timeSlotParam=new TimeSlotParam();
        timeSlotParam.setTutorType(instantClassCard.getTutorType());
        timeSlotParam.setComboType(instantClassCard.getComboType());
        timeSlotParam.setOrderId(instantClassCard.getOrderId());
        timeSlotParam.setProductType(instantClassCard.getProductType());
        timeSlotParam.setStudentId(instantClassCard.getStudentId());
        return timeSlotParam;
    }
}
