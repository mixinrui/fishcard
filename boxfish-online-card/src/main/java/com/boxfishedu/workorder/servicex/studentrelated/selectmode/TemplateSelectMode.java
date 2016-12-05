package com.boxfishedu.workorder.servicex.studentrelated.selectmode;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/9/22.
 * 模板选时间方式
 */
@Component
public class TemplateSelectMode implements SelectMode {

    public final static Logger logger = LoggerFactory.getLogger(TemplateSelectMode.class);
    public final static int DEFAULT_EXCHANGE_NUM_PER_WEEK = 2;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    // TODO 修改
    @Override
    public List<WorkOrder> initWorkOrderList(TimeSlotParam timeSlotParam, SelectMode selectMode, List<Service> services) {
        // 先初始化参数
        SelectTemplateParam selectTemplateParam = createSelectTemplateParam(timeSlotParam, services);
        // 单次选择模板次数
        int count = selectTemplateParam.getCount();
        logger.info("selectTemplate [{}]", selectTemplateParam);
        List<WorkOrder> workOrders = new ArrayList<>();
        Queue<ServiceChoice> serviceQueue = createServiceChoice(services);


        // 获取序列集合
        Map<Long, AtomicInteger> sequences = createSequences(workOrderJpaRepository, services,
                (Objects.equals(timeSlotParam.getComboType(), ComboTypeToRoleId.EXCHANGE.name())));

        for (int i = 0, loopOfWeek = selectTemplateParam.getLoopOfWeek(), c = 0; i < loopOfWeek; i++) {
            for (int j = 0, numPerWeek = selectTemplateParam.getNumPerWeek(); j < numPerWeek; j++) {
                if(++c > count) {
                    break;
                }
                Service service = services.get(choice(serviceQueue));
                int index = sequences.get(service.getId()).incrementAndGet();
                List<SelectedTime> selectedTimes = timeSlotParam.getSelectedTimes();
                WorkOrder workOrder = initWorkOrder(service, index, timeSlotParam.getSelectedTimes().get(j).getTimeSlotId());
                TimeSlots timeSlots = teacherStudentRequester.getTimeSlot(timeSlotParam.getSelectedTimes().get(j).getTimeSlotId());
                String startTimeString = selectedTimes.get(j).getSelectedDate() + " " + timeSlots.getStartTime();
                String endTimeString = selectedTimes.get(j).getSelectedDate() + " " + timeSlots.getEndTime();
                DateFormat sdf = ThreadLocalUtil.dateTimeFormatThreadLocal.get();
                try {
                    Date startTime = sdf.parse(startTimeString);
                    Date endTime = sdf.parse(endTimeString);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(startTime);
                    //service的validate day来自sku的validate day
                    calendar.add(Calendar.DATE, i * Calendar.DAY_OF_WEEK);
                    workOrder.setStartTime(calendar.getTime());
                    calendar.setTime(endTime);
                    calendar.add(Calendar.DATE, i * Calendar.DAY_OF_WEEK);
                    workOrder.setEndTime(calendar.getTime());
                } catch (Exception ex) {
                    throw new BusinessException("生成鱼卡时日期选择出现异常");
                }
                workOrders.add(workOrder);
            }
        }
        return workOrders;
    }


    public static SelectTemplateParam createSelectTemplateParam(TimeSlotParam timeSlotParam, List<Service> services) {
        // 求总和
        int count = services.stream().collect(Collectors.summingInt(Service::getAmount));
        // 兑换默认为1周两次
        if(Objects.equals(timeSlotParam.getComboTypeEnum(), ComboTypeToRoleId.EXCHANGE)) {
            int loopOfWeek = count <=2 ? count : (count + DEFAULT_EXCHANGE_NUM_PER_WEEK - 1) / DEFAULT_EXCHANGE_NUM_PER_WEEK;
            // 小于等于2的每周一次课
            int numPerWeek = count <= 2 ? 1: DEFAULT_EXCHANGE_NUM_PER_WEEK;
            return new SelectTemplateParam(loopOfWeek, numPerWeek, count);
        } else {
            if(StringUtils.equals(timeSlotParam.getComboType(), ComboTypeToRoleId.INTELLIGENT.name())) {
                int loopOfWeek = services.get(0).getComboCycle();
                int numPerWeek = services.get(0).getCountInMonth();
                return new SelectTemplateParam(loopOfWeek, numPerWeek, count);
            } else {
                int loopOfWeek = services.stream().collect(Collectors.summingInt(Service::getComboCycle));
                int per = (count / loopOfWeek == 0 ? 1 : count / loopOfWeek);
                logger.info("weekStrategy= loopOfWeek:[{}],per:[{}]", loopOfWeek, per);
                return new SelectTemplateParam((count + per -1) / per, per, count);
            }
        }
    }

}
