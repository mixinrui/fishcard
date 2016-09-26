package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/9/26.
 */
@Component
public class SelectModeFactory {


    private Map<Class, SelectMode> selectModeMap;


    @Autowired
    public SelectModeFactory(List<SelectMode> selectModeList) {
        selectModeMap = new HashMap<>();
        selectModeList.forEach(selectMode -> selectModeMap.put(selectMode.getClass(), selectMode));
    }


    public SelectMode createSelectMode(TimeSlotParam timeSlotParam) {
        // 默认是template
        if (Objects.isNull(timeSlotParam.getSelectMode()) ||
                Objects.equals(timeSlotParam.getSelectMode(), SelectMode.TEMPLATE)) {
            return selectModeMap.get(TemplateSelectMode.class);
        } else {
            return selectModeMap.get(UserDefinedSelectMode.class);
        }
    }
}
