package com.boxfishedu.workorder.servicex.dataanalysis;

import com.boxfishedu.workorder.common.bean.AppPointRecordEventEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.mongo.NetNanlysisInfoMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.NetPingAnalysisInfo;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.DataAnalysisRequester;
import com.boxfishedu.workorder.requester.resultbean.EventResultBean;
import com.boxfishedu.workorder.requester.resultbean.NetAnalysisBean;
import com.boxfishedu.workorder.requester.resultbean.NetSourceBean;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.requester.DataAnalysisLogParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by hucl on 16/8/6.
 */
@Component
public class FetchHeartBeatServiceX {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DataAnalysisRequester dataAnalysisRequester;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private NetNanlysisInfoMorphiaRepository netNanlysisInfoMorphiaRepository;

    @Autowired
    private ThreadPoolManager threadPoolManager;


    public EventResultBean fetchHeartBeatLog(DataAnalysisLogParam dataAnalysisLogParam) {
        return dataAnalysisRequester.fetchHeartBeatLog(dataAnalysisLogParam);
    }

    public boolean isOnline(DataAnalysisLogParam dataAnalysisLogParam) {
        try {
            String startDateStr = DateUtil.Date2String(new Date(dataAnalysisLogParam.getStartTime()));
            String endDateStr = DateUtil.Date2String(new Date(dataAnalysisLogParam.getEndTime()));
            List<EventResultBean.EventResult> eventResults = fetchHeartBeatLog(dataAnalysisLogParam).getContent();
            boolean result = CollectionUtils.isNotEmpty(eventResults);
            logger.info("@FetchHeartBeatServiceX###isOnline###判断当前用户[{}]在指定时间startDate[{}],endDate[{}]是否在线[{}];",
                        dataAnalysisLogParam.getUserId(), startDateStr, endDateStr, result);
            return result;
        } catch (Exception ex) {
            logger.error("@isOnline分析数据有问题,影响结果");
            return false;
        }
    }

    //直接获取详细信息
    public JsonResultModel getNetPingDetail(Long cardId, String role, Pageable pageable) {
        return JsonResultModel.newJsonResultModel(
                this.getNetPingDetailContent(cardId, role, pageable));
    }

    public NetSourceBean getNetPingDetailContent(Long cardId, String role, Pageable pageable) {
        WorkOrder workOrder = workOrderService.findOne(cardId);
        return this.getNetPingDetailContent(workOrder, role, pageable);
    }

    //直接获取详细信息
    public NetSourceBean getNetPingDetailContent(WorkOrder workOrder, String role, Pageable pageable) {
        DataAnalysisLogParam dataAnalysisLogParam = new DataAnalysisLogParam();
        dataAnalysisLogParam.setStartTime(workOrder.getStartTime().getTime());
        dataAnalysisLogParam.setEndTime(workOrder.getEndTime().getTime());
        dataAnalysisLogParam.setEvent(AppPointRecordEventEnum.ONLINE_COURSE_HEARTBEAT.value());
        if (role.equals("student")) {
            dataAnalysisLogParam.setUserId(workOrder.getStudentId());
        } else {
            dataAnalysisLogParam.setUserId(workOrder.getTeacherId());
        }
        return dataAnalysisRequester.getNetSourceBean(dataAnalysisLogParam, pageable);
    }

    public NetSourceBean getAllNetPing(WorkOrder workOrder, String role) {
        Pageable page = new PageRequest(0, Byte.MAX_VALUE);
        return this.getNetPingDetailContent(workOrder, role, page);
    }

    public void persistAnalysis(WorkOrder workOrder) {
        logger.debug("@persistAnalysis#开始初始化网络情况#参数[{}]", workOrder);
        this.persistAnalysis(workOrder.getId(), workOrder.getStudentId(), this.getAllNetPing(workOrder, "student"));
        this.persistAnalysis(workOrder.getId(), workOrder.getTeacherId(), this.getAllNetPing(workOrder, "teacher"));
    }

    public void persistAnalysisAsync(WorkOrder workOrder) {
        if(workOrder.getStartTime().after(new Date())){
            return;
        }
        if(workOrder.getStatus()== FishCardStatusEnum.COURSE_ASSIGNED.getCode()){
            return;
        }
        threadPoolManager.execute(new Thread(() -> {
            this.persistAnalysis(workOrder);
        }));
    }

    public void persistAnalysis(Long cardId, Long userId, NetSourceBean netSourceBean) {
        NetAnalysisBean netAnalysisBean = new NetAnalysisBean();
        netAnalysisBean.netAdapter(netSourceBean);

        NetPingAnalysisInfo netPingAnalysisInfo = this.getNetAnalysis(cardId, userId);
        if (Objects.isNull(netPingAnalysisInfo)) {
            netPingAnalysisInfo = new NetPingAnalysisInfo();
        }

        netPingAnalysisInfo.wrapIntoBean(netAnalysisBean, userId, cardId);

        netNanlysisInfoMorphiaRepository.save(netPingAnalysisInfo);
    }

    public NetPingAnalysisInfo getNetAnalysis(Long cardId, String role) {
        WorkOrder workOrder = workOrderService.findOne(cardId);
        if (role.equals("student")) {
            return this.getNetAnalysis(cardId, workOrder.getStudentId());
        } else if (role.equals("teacher")) {
            return this.getNetAnalysis(cardId, workOrder.getTeacherId());
        } else {
            throw new BusinessException("角色参数不正确");
        }
    }

    public NetPingAnalysisInfo getNetAnalysis(Long cardId, Long userId) {
        return netNanlysisInfoMorphiaRepository.queryByCardIdAndUserId(cardId, userId);
    }
}
