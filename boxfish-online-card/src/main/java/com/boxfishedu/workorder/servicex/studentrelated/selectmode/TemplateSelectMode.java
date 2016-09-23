package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/9/22.
 * 模板选时间方式
 */
public class TemplateSelectMode extends SelectMode {

    public final static Logger logger = LoggerFactory.getLogger(TemplateSelectMode.class);
    public final static int DEFAULT_EXCHANGE_NUM_PER_WEEK = 2;
    int loopOfWeek;
    int numPerWeek;
    int count;

    private TemplateSelectMode(int loopOfWeek, int numPerWeek, int count) {
        this.loopOfWeek = loopOfWeek;
        this.numPerWeek = numPerWeek;
        this.count = count;
    }

    @Override
    public int getLoopOfWeek() {
        return loopOfWeek;
    }

    @Override
    public int getNumPerWeek() {
        return numPerWeek;
    }

    @Override
    public int getCount() {
        return count;
    }


    public static TemplateSelectMode createTemplateSelectMode(TimeSlotParam timeSlotParam, List<Service> services) {
        int count = services.stream().collect(Collectors.summingInt(Service::getAmount));
        // 兑换默认为1周两次
        if(Objects.equals(timeSlotParam.getComboTypeEnum(), ComboTypeToRoleId.EXCHANGE)) {
            int loopOfWeek = (count + DEFAULT_EXCHANGE_NUM_PER_WEEK - 1) / DEFAULT_EXCHANGE_NUM_PER_WEEK;
            int numPerWeek = count == 1 ? 1: DEFAULT_EXCHANGE_NUM_PER_WEEK;
            return new TemplateSelectMode(loopOfWeek, numPerWeek, count);
        }
        int loopOfWeek = services.stream().collect(Collectors.summingInt(Service::getComboCycle));
        int per = count / loopOfWeek == 0 ? 1 : count / loopOfWeek;
        logger.info("weekStrategy= loopOfWeek:[{}],per:[{}]", loopOfWeek, per);
        return new TemplateSelectMode((count + per -1) / per, per, count);
    }
}
