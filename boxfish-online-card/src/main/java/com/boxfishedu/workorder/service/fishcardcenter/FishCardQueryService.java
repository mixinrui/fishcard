package com.boxfishedu.workorder.service.fishcardcenter;

import com.boxfishedu.mall.enums.OrderChannelDesc;
import com.boxfishedu.workorder.common.bean.*;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderLogJpaRepository;
import com.boxfishedu.workorder.entity.mongo.ContinousAbsenceRecord;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.absencendeal.AbsenceDealService;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/6/16.
 */
@Component
@SuppressWarnings("ALL")
public class FishCardQueryService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AbsenceDealService absenceDealService;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    public Long filterFishCardsCount(FishCardFilterParam fishCardFilterParam) {
        String prefix = "select count(wo) ";
        String sql = prefix + getFilterSql(fishCardFilterParam);
        Query query = getFilterQuery(sql, fishCardFilterParam, entityManager);
        Long count = (Long) query.getSingleResult();
        return count;
    }

    public List<WorkOrder> filterFishCards(FishCardFilterParam fishCardFilterParam, Pageable pageable) {
        String prefix = "select wo ";
        String sql = prefix + getFilterSql(fishCardFilterParam);
        Query query = getFilterQuery(sql, fishCardFilterParam, entityManager);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<WorkOrder> workOrders = query.getResultList();
        for (WorkOrder workOrder : workOrders) {
            workOrder.setOrderCode(workOrder.getService().getOrderCode());
            workOrder.setStatusDesc(FishCardStatusEnum.getDesc((workOrder.getStatus())));
            workOrder.setMakeUpOrNot("否");
            workOrder.setIdDesc(workOrder.getId().toString());

            if (null != workOrder.getMakeUpFlag() && 0 != workOrder.getMakeUpFlag()) {
                workOrder.setMakeUpOrNot("是");
            }

            if (null != workOrder.getOrderChannel()) {
                if (workOrder.getOrderChannel().equals(OrderChannelDesc.STANDARD.getCode())) {
                    //终极梦想
                    if (workOrder.getComboType().equals(OrderChannelDesc.CHINESE.getCode()) ||
                            (workOrder.getComboType().equals(OrderChannelDesc.INTELLIGENT.getCode()) && workOrder.getService().getTutorType().equals(TutorTypeEnum.FRN.name()))) {
                        workOrder.setOrderTypeDesc(OrderChannelDesc.CHINESE.getDesc());
                        //考试指导
                    } else if (workOrder.getComboType().equals(OrderChannelDesc.INTELLIGENT.getCode()) && workOrder.getService().getTutorType().equals(TutorTypeEnum.CN.name())) {
                        workOrder.setOrderTypeDesc(OrderChannelDesc.INTELLIGENT.getDesc());
                    } else {
                        workOrder.setOrderTypeDesc(OrderChannelDesc.get(workOrder.getComboType()).getDesc());
                    }

                } else {
                    workOrder.setOrderTypeDesc(OrderChannelDesc.get(workOrder.getOrderChannel()).getDesc());
                }
            }
            workOrder.setTeachingType(workOrder.getSkuId());

            if (workOrder.getParentId() != null && workOrder.getParentId() != 0l) {
                String idDesc = workOrder.getParentRootId() + "";
                for (int i = 0; i < workOrder.getMakeUpSeq(); i++) {
                    idDesc += "B";
                }
                workOrder.setIdDesc(idDesc);
            }
            if (StringUtils.isNotEmpty(workOrder.getService().getComboType())) {
//                if (workOrder.getService().getComboType().equals(ComboTypeEnum.EXCHANGE.toString())) {
                    if (workOrder.getStatus() < FishCardStatusEnum.WAITFORSTUDENT.getCode()) {
                        LocalDateTime beginLocalDate = LocalDateTime.ofInstant(DateUtil.date2SimpleDate(new Date()).toInstant(), ZoneId.systemDefault()).minusHours(24);
                        if (workOrder.getStartTime().after(DateUtil.localDate2Date(beginLocalDate))) {
                            //处于冻结
                            if (workOrder.getIsFreeze() == 1) {
                                workOrder.setUnfreezeBtnShowFlag(Boolean.TRUE);
                            } else {
                                workOrder.setFreezeBtnShowFlag(Boolean.TRUE);
                            }
                        }
//                    }

                }
            }
        }

        filterTeacherRequested(workOrders);

        return workOrders;
    }

    private String getFilterSql(FishCardFilterParam fishCardFilterParam) {
        StringBuilder sql = new StringBuilder("from WorkOrder wo where wo.startTime between :begin and :end ");

        if (null != fishCardFilterParam.getOrderType()) {
            if (fishCardFilterParam.getOrderType().equals(OrderChannelDesc.OVERALL.getCode())
                    ||
                    fishCardFilterParam.getOrderType().equals(OrderChannelDesc.FOREIGN.getCode())
                    ) {
                sql.append(" and wo.comboType=:orderChannel and wo.orderChannel= '").append(OrderChannelDesc.STANDARD.getCode()).append("' ");
            } else if (fishCardFilterParam.getOrderType().equals(OrderChannelDesc.CHINESE.getCode())) {       // 终极梦想
                sql.append(" and (wo.comboType=:orderChannel or ( wo.comboType= '").append(OrderChannelDesc.INTELLIGENT.getCode()).append("' ").
                        append(" and  wo.service.tutorType= '").append(TutorTypeEnum.FRN).append("' )  )  and wo.orderChannel= '").append(OrderChannelDesc.STANDARD.getCode()).append("'");
            } else if (fishCardFilterParam.getOrderType().equals(OrderChannelDesc.INTELLIGENT.getCode())) { // 考试指导
                sql.append(" and wo.comboType=:orderChannel  ").append(" and  wo.service.tutorType= '").append(TutorTypeEnum.CN).append("'   and wo.orderChannel= '").append(OrderChannelDesc.STANDARD.getCode()).append("'");
            } else {
                sql.append(" and wo.orderChannel=:orderChannel ");
            }

        }
        if (null != fishCardFilterParam.getConfirmFlag()) {
            if ("1".equals(fishCardFilterParam.getConfirmFlag())) {
                sql.append(" and (wo.confirmFlag=:confirmFlag or wo.confirmFlag is null )  ");
            } else {
                sql.append(" and wo.confirmFlag=:confirmFlag ");
            }
        }

        if ("before".equals(fishCardFilterParam.getRechargeType())) {
            sql.append(" and wo.statusRecharge = :statusRecharge ");  //
        }

        if ("after".equals(fishCardFilterParam.getRechargeType())) {
            if (null == fishCardFilterParam.getRechargeValue()) {
                sql.append(" and wo.statusRecharge > :statusRecharge ");  //
            } else {
                sql.append(" and wo.statusRecharge = :statusRechargeValue ");  //
            }
        }

        // 中外教
        if (null != fishCardFilterParam.getTeachingType()) {
            sql.append(" and wo.skuId = :teachingType ");
        }

        if (null != fishCardFilterParam.getCreateBeginDateFormat()) {
            sql.append(" and wo.createTime>=:createbegin ");
        }
        if (null != fishCardFilterParam.getCreateEndDateFormat()) {
            sql.append(" and wo.createTime<=:createend ");
        }

//        if(null!=fishCardFilterParam.getStatus()){
//            sql.append("and status in (:status )");
//        }
        if (null != fishCardFilterParam.getOrderCode()) {
            sql.append("and orderCode=:orderCode ");
        }
        if (null != fishCardFilterParam.getId()) {
            sql.append("and id=:id ");
        }
        if (null != fishCardFilterParam.getStudentId()) {
            sql.append("and studentId=:studentId ");
        }

        if (null != fishCardFilterParam.getTeacherId()) {
            sql.append("and teacherId=:teacherId ");
        }

        if (null != fishCardFilterParam.getTeacherName() && StringUtils.isNotEmpty(fishCardFilterParam.getTeacherName().trim())) {
            sql.append("and teacherName like '%").append(fishCardFilterParam.getTeacherName()).append("%' ");
        }

        if (null != fishCardFilterParam.getCourseType() && StringUtils.isNotEmpty(fishCardFilterParam.getCourseType())) {
            sql.append("and courseType in (").append(splitCourseType(fishCardFilterParam.getCourseType())).append(") ");
        }

        if (null != fishCardFilterParam.getStatuses() && StringUtils.isNotEmpty(fishCardFilterParam.getStatuses())) {
            sql.append("and status in (").append(splitCourseTypeString(fishCardFilterParam.getStatuses())).append(") ");
        }

        if (null != fishCardFilterParam.getContineAbsenceNum()) {
            List<ContinousAbsenceRecord> continousAbsenceRecords = absenceDealService.queryByComboTypeAndContinusAbsenceNum(ComboTypeEnum.EXCHANGE.toString(), fishCardFilterParam.getContineAbsenceNum());
            if (!CollectionUtils.isEmpty(continousAbsenceRecords)) {
                StringBuilder builder = new StringBuilder();
                int i = 0;
                for (ContinousAbsenceRecord continousAbsenceRecord : continousAbsenceRecords) {
                    if (i == continousAbsenceRecords.size() - 1) {
                        builder.append(continousAbsenceRecord.getStudentId());
                    } else {
                        builder.append(continousAbsenceRecord.getStudentId() + ",");
                    }
                    i++;
                }
                sql.append("and student_id in (").append(builder).append(") ");
            } else {
                sql.append("and student_id in (").append(-1).append(") ");
            }
            sql.append("and orderChannel=:comboType ");
        }

        if (StringUtils.isNotEmpty(fishCardFilterParam.getDemoType())) {
            if (fishCardFilterParam.getDemoType().trim().equals("true")) {
                sql.append("and orderId=:orderId ");
            } else {
                sql.append("and orderId !=:orderId ");
            }
        } else {
            sql.append("and orderId !=:orderId ");
        }


        if (null != fishCardFilterParam.getMakeUpFlag()) {
            if (fishCardFilterParam.getMakeUpFlag()) {
                sql.append(" and parentId is not null ");
            } else {
                sql.append(" and parentId is  null  ");
            }
        }


        if (null != fishCardFilterParam.getStartTimeSort()) {
            sql.append("order by wo.startTime   ").append(fishCardFilterParam.getStartTimeSort().toLowerCase());
        }

        if (null != fishCardFilterParam.getActualStartTimeSort()) {
            sql.append("order by wo.actualStartTime ").append(fishCardFilterParam.getActualStartTimeSort());
        }

        if (null != fishCardFilterParam.getStartTimeSort() && null != fishCardFilterParam.getActualStartTimeSort()) {
            sql.append("order by wo.teacherId asc , wo.createTime desc");
        }

        if (null != fishCardFilterParam.getTeacherNameSort()) {
            sql.append(" order by teacherName ").append(fishCardFilterParam.getTeacherNameSort());
        }

        return sql.toString();
    }


    public void filterTeacherRequested(List<WorkOrder> workOrders) {
        for (WorkOrder workOrder : workOrders) {
            if (workOrder.getStatus() != FishCardStatusEnum.STUDENT_ENTER_ROOM.getCode()
                    && workOrder.getStatus() != FishCardStatusEnum.READY.getCode()) {
                break;
            }
            List<WorkOrderLog> workOrderLogs = workOrderLogService.queryByWorkId(workOrder.getId());

            workOrder.setHaveTeacherRequested(Boolean.FALSE);

            for (WorkOrderLog workOrderLog : workOrderLogs) {
                if (workOrderLog.getStatus() == FishCardStatusEnum.CONNECTED.getCode()
                        || workOrderLog.getStatus() == FishCardStatusEnum.WAITFORSTUDENT.getCode()
                        || workOrderLog.getStatus() == FishCardStatusEnum.ONCLASS.getCode()) {
                    workOrder.setHaveTeacherRequested(Boolean.TRUE);
                    break;
                }
            }
        }
    }

    private String splitCourseType(String courseType) {
        String condition = "";
        String[] course = courseType.split(",");
        for (String s : course) {
            condition += "'" + s.toUpperCase() + "',";
        }
        return condition.substring(0, condition.length() - 1);
    }

    private String splitCourseTypeString(String courseType) {
        String condition = "";
        String[] course = courseType.split(",");
        for (String s : course) {
            condition += s.toUpperCase() + ",";
        }
        return condition.substring(0, condition.length() - 1);
    }

    private Query getFilterQuery(String sql, FishCardFilterParam fishCardFilterParam, EntityManager entityManager) {
        Query query = entityManager.createQuery(sql);
        query.setParameter("begin", fishCardFilterParam.getBeginDateFormat());
        query.setParameter("end", fishCardFilterParam.getEndDateFormat());

        if (null != fishCardFilterParam.getId()) {
            query.setParameter("id", fishCardFilterParam.getId());
        }
        // 订单类型
        if (null != fishCardFilterParam.getOrderType()) {
            query.setParameter("orderChannel", fishCardFilterParam.getOrderType());
        }
        if (null != fishCardFilterParam.getContineAbsenceNum()) {
            query.setParameter("comboType", ComboTypeEnum.EXCHANGE.toString());
        }

        if (null != fishCardFilterParam.getCreateBeginDateFormat()) {
            query.setParameter("createbegin", fishCardFilterParam.getCreateBeginDateFormat());
        }
        if (null != fishCardFilterParam.getCreateEndDateFormat()) {
            query.setParameter("createend", fishCardFilterParam.getCreateEndDateFormat());
        }

//        if(null!=fishCardFilterParam.getStatus()){
//            query.setParameter("status",fishCardFilterParam.getStatus());
//        }
        if (null != fishCardFilterParam.getOrderCode()) {
            query.setParameter("orderCode", fishCardFilterParam.getOrderCode());
        }
        if (null != fishCardFilterParam.getStudentId()) {
            query.setParameter("studentId", fishCardFilterParam.getStudentId());
        }
        if (null != fishCardFilterParam.getTeacherId()) {
            query.setParameter("teacherId", fishCardFilterParam.getTeacherId());
        }

        if (null != fishCardFilterParam.getConfirmFlag()) {
            query.setParameter("confirmFlag", fishCardFilterParam.getConfirmFlag());
        }

        if ("before".equals(fishCardFilterParam.getRechargeType())) {
            query.setParameter("statusRecharge", FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode());
        }

        if ("after".equals(fishCardFilterParam.getRechargeType())) {
            if (null == fishCardFilterParam.getRechargeValue()) {
                query.setParameter("statusRecharge", FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode());
            } else {
                query.setParameter("statusRechargeValue", fishCardFilterParam.getRechargeValue());
            }
        }

        if (null != fishCardFilterParam.getTeachingType()) {

            query.setParameter("teachingType", fishCardFilterParam.getTeachingType());
        }


//        if(null!=fishCardFilterParam.getTeacherName()){
//            query.setParameter("teacherName",fishCardFilterParam.getTeacherName());
//        }
        query.setParameter("orderId", Long.MAX_VALUE);
        return query;
    }


}
