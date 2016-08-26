package com.boxfishedu.workorder.web.param;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/4/26.
 */
@Data
public class TimeSlotParam {
    private Long orderId;
    private Integer productType;
    private String tutorType;
    private String comboType;
    @JsonIgnore
    private Long studentId;
    private List<SelectedTime> selectedTimes;

    public ComboTypeToRoleId getComboTypeEnum() {
        return ComboTypeToRoleId.resolve(comboType);
    }
}
