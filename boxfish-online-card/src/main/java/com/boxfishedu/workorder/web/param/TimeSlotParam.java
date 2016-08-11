package com.boxfishedu.workorder.web.param;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/4/26.
 */
@Data
public class TimeSlotParam {
    private Long orderId;
    private String comboType;
    private List<SelectedTime> selectedTimes;
    public ComboTypeToRoleId getComboType() {
        return ComboTypeToRoleId.resolve(comboType);
    }
}
