package com.boxfishedu.workorder.servicex.studentrelated;


import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by hucl on 17/1/16.
 */
@Component
public class SmallClassSuperStuService {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private DataCollectorService dataCollectorService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Transactional
    public Service createSmallClassSuperService() {

        List<Service> services = serviceJpaRepository.findByOrderId(this.virtualOrderId());
        if (CollectionUtils.isEmpty(services)) {
            Service service  =  new Service();
            service.setCreateTime(new Date());
            service.setStudentId(0l);
            service.setOrderId(this.virtualOrderId());
            service.setClassSize(0);
            service.setUserType(0);
            service.setComboType(ComboTypeEnum.FSCF.name());
            service.setProductType(1001);
            service.setTutorType(TutorTypeEnum.FRN.name());
            service.setAmount(1);
            return serviceJpaRepository.save(service);
        }
        return services.get(0);

    }

    public Long virtualOrderId() {
        return Long.MAX_VALUE - 10;
    }




   // @Transactional
    public void restoreClassForSmallClass(WorkOrder workOrder, CourseSchedule courseSchedule, SmallClass smallClass) {
        logger.debug("@restoreClassForSmallClass#鱼卡[{}]", JacksonUtil.toJSon(workOrder));
        // 更新workOrder 和 courseSchedule
        //workOrderService.updateWorkOrderAndSchedule(workOrder, courseSchedule);

        changeWorkOrderLog(workOrder, "分配课程");
        changeWorkOrderLog(workOrder, "分配老师");

        // 添加小班课群主关系
        smallClass.setAllStudentIds(Lists.newArrayList(workOrder.getStudentId()));
        FishCardGroupsInfo fishCardGroupsInfo = courseOnlineRequester.buildsmallClassChatRoom(smallClass);

        String groupId = fishCardGroupsInfo == null ? "" : fishCardGroupsInfo.getGroupId();  //群主Id
        Long chatRoomId = fishCardGroupsInfo == null ? null : fishCardGroupsInfo.getChatRoomId();//房间号
        logger.info(String.format("auToMakeClassesForSmallClassFishCardGroupsInfo:%s__ %s", groupId, chatRoomId));

        //调用首页接口
        dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());

    }


    private void changeWorkOrderLog(WorkOrder workOrder, String content) {
        WorkOrderLog workOrderLog = new WorkOrderLog();
        workOrderLog.setCreateTime(new Date());
        workOrderLog.setWorkOrderId(workOrder.getId());
        workOrderLog.setStatus(workOrder.getStatus());
        workOrderLog.setContent(String.format("%s[小班课][添加超级用户]", content));
        workOrderLogService.save(workOrderLog);
    }

}
