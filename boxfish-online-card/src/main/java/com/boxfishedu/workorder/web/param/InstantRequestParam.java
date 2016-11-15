package com.boxfishedu.workorder.web.param;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

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

    public static TimeSlotParam timeSlotParamAdapter(InstantRequestParam instantRequestParam){
        TimeSlotParam timeSlotParam=new TimeSlotParam();
        try {
            BeanUtils.copyProperties(timeSlotParam, instantRequestParam);
        }
        catch (Exception ex){
            throw new BusinessException("参数转换失败");
        }
        return timeSlotParam;
    }

    public static enum SelectModeEnum{
        COURSE_SCHEDULE_ENTERANCE(0,"课程表入口"),
        OTHER_ENTERANCE(1,"其他入口"),
        UNKNOWN(2,"未知");
        private int code;
        private String desc;

        SelectModeEnum(int code, String desc){
            this.desc = desc;
            this.code = code;
        }

        public static SelectModeEnum getSelectMode(int code){
            for (SelectModeEnum selectModeEnum: SelectModeEnum.values()){
                if (selectModeEnum.code == code)
                    return selectModeEnum;
            }
            return null;
        }

        public int getCode(){
            return this.code;
        }
        public String getDesc(){
            for (SelectModeEnum selectModeEnum: SelectModeEnum.values()){
                if (selectModeEnum.code == this.getCode())
                    return selectModeEnum.desc;
            }
            return null;
        }
    }
}
