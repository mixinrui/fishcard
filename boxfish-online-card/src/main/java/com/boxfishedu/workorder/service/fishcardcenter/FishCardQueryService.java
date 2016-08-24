package com.boxfishedu.workorder.service.fishcardcenter;

import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.base.BaseService;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Created by hucl on 16/6/16.
 */
@Component
public class FishCardQueryService extends BaseService<WorkOrder, WorkOrderJpaRepository, Long> {
    @Autowired
    private EntityManager entityManager;

    public Long filterFishCardsCount(FishCardFilterParam fishCardFilterParam){
        String prefix="select count(wo) ";
        String sql=prefix+getFilterSql(fishCardFilterParam);
        Query query = getFilterQuery(sql,fishCardFilterParam,entityManager);
        Long count=(Long)query.getSingleResult();
        return count;
    }

    public List<WorkOrder> filterFishCards(FishCardFilterParam fishCardFilterParam, Pageable pageable){
        String prefix="select wo ";
        String sql=prefix+getFilterSql(fishCardFilterParam);
        Query query = getFilterQuery(sql,fishCardFilterParam,entityManager);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<WorkOrder> workOrders=query.getResultList();
        for (WorkOrder workOrder : workOrders) {
            workOrder.setOrderCode(workOrder.getService().getOrderCode());
            workOrder.setStatusDesc(FishCardStatusEnum.getDesc((workOrder.getStatus())));
            workOrder.setSkuId(workOrder.getService().getSkuId());
            workOrder.setMakeUpOrNot("否");
            workOrder.setIdDesc(workOrder.getId().toString());
            if(null!=workOrder.getMakeUpFlag()&&0!=workOrder.getMakeUpFlag()){
                workOrder.setMakeUpOrNot("是");
            }
            if(ConstantUtil.SKU_EXTRA_VALUE==workOrder.getSkuIdExtra()){
                workOrder.setTeachingType(TeachingType.WAIJIAO.getCode());
            }
            else{
                workOrder.setTeachingType(TeachingType.ZHONGJIAO.getCode());
            }
            if (workOrder.getParentId()!=null&&workOrder.getParentId()!=0l){
                String idDesc=workOrder.getParentRootId()+"";
                for(int i=0;i<workOrder.getMakeUpSeq();i++){
                    idDesc+="B";
                }
                workOrder.setIdDesc(idDesc);
            }
        }
        return workOrders;
    }

    private String getFilterSql(FishCardFilterParam fishCardFilterParam){
        StringBuilder sql=new StringBuilder("from WorkOrder wo where wo.startTime between :begin and :end ");


        if(null !=fishCardFilterParam.getConfirmFlag()){
            if("1".equals(fishCardFilterParam.getConfirmFlag())){
                sql.append(" and (wo.confirmFlag=:confirmFlag or wo.confirmFlag !='0' )  ");
            }else {
                sql.append(" and wo.confirmFlag=:confirmFlag ");
            }
        }

        if("before".equals(fishCardFilterParam.getRechargeType())){
            sql.append(" and wo.statusRecharge = :statusRecharge ");  //
        }

        if("after".equals(fishCardFilterParam.getRechargeType())){
            if(null == fishCardFilterParam.getRechargeValue()) {
                sql.append(" and wo.statusRecharge > :statusRecharge ");  //
            }else {
                sql.append(" and wo.statusRecharge = :statusRechargeValue ");  //
            }
        }



        if(null!=fishCardFilterParam.getCreateBeginDateFormat()){
            sql.append(" and wo.createTime>=:createbegin ");
        }
        if(null!=fishCardFilterParam.getCreateEndDateFormat()){
            sql.append(" and wo.createTime<=:createend ");
        }

//        if(null!=fishCardFilterParam.getStatus()){
//            sql.append("and status in (:status )");
//        }
        if(null!=fishCardFilterParam.getOrderCode()){
            sql.append("and orderCode=:orderCode ");
        }
        if(null!=fishCardFilterParam.getStudentId()){
            sql.append("and studentId=:studentId ");
        }

        if(null!=fishCardFilterParam.getTeacherId()){
            sql.append("and teacherId=:teacherId ");
        }

        if(null!=fishCardFilterParam.getTeacherName() && StringUtils.isNotEmpty(fishCardFilterParam.getTeacherName().trim())){
            sql.append("and teacherName like '%").append(fishCardFilterParam.getTeacherName()).append("%' ");
        }

        if(null!=fishCardFilterParam.getCourseType() &&  StringUtils.isNotEmpty(  fishCardFilterParam.getCourseType() )){
            sql.append("and courseType in (").append(splitCourseType(fishCardFilterParam.getCourseType())).append(") ");
        }

        if(null!=fishCardFilterParam.getStatuses() &&  StringUtils.isNotEmpty(  fishCardFilterParam.getStatuses() )){
            sql.append("and status in (").append(splitCourseTypeString(fishCardFilterParam.getStatuses())).append(") ");
        }

        sql.append("and orderId !=:orderId ");


        if(null!=fishCardFilterParam.getStartTimeSort()){
            sql.append("order by wo.startTime   ").append(fishCardFilterParam.getStartTimeSort().toLowerCase());
        }

        if( null!=fishCardFilterParam.getActualStartTimeSort()){
            sql.append("order by wo.actualStartTime ").append(fishCardFilterParam.getActualStartTimeSort());
        }

        if(null!=fishCardFilterParam.getStartTimeSort()  &&  null!=fishCardFilterParam.getActualStartTimeSort()){
            sql.append("order by wo.teacherId asc , wo.createTime desc");
        }

        return sql.toString();
    }

    private String splitCourseType(String courseType){
        String condition = "";
        String[] course = courseType.split(",");
        for(String s: course){
            condition+="'"+s.toUpperCase()+"',";
        }
        return condition.substring(0,condition.length()-1);
    }

    private String splitCourseTypeString(String courseType){
        String condition = "";
        String[] course = courseType.split(",");
        for(String s: course){
            condition+=s.toUpperCase()+",";
        }
        return condition.substring(0,condition.length()-1);
    }

    private Query getFilterQuery(String sql, FishCardFilterParam fishCardFilterParam, EntityManager entityManager){
        Query query = entityManager.createQuery(sql);
        query.setParameter("begin",fishCardFilterParam.getBeginDateFormat());
        query.setParameter("end",fishCardFilterParam.getEndDateFormat());

        if(null!=fishCardFilterParam.getCreateBeginDateFormat()){
            query.setParameter("createbegin",fishCardFilterParam.getCreateBeginDateFormat());
        }
        if(null!=fishCardFilterParam.getCreateEndDateFormat()){
            query.setParameter("createend",fishCardFilterParam.getCreateEndDateFormat());
        }

//        if(null!=fishCardFilterParam.getStatus()){
//            query.setParameter("status",fishCardFilterParam.getStatus());
//        }
        if(null!=fishCardFilterParam.getOrderCode()){
            query.setParameter("orderCode",fishCardFilterParam.getOrderCode());
        }
        if(null!=fishCardFilterParam.getStudentId()){
            query.setParameter("studentId",fishCardFilterParam.getStudentId());
        }
        if(null!=fishCardFilterParam.getTeacherId()){
            query.setParameter("teacherId",fishCardFilterParam.getTeacherId());
        }

        if(null !=fishCardFilterParam.getConfirmFlag()){
            query.setParameter("confirmFlag",fishCardFilterParam.getConfirmFlag());
        }

        if("before".equals(fishCardFilterParam.getRechargeType())){
            query.setParameter("statusRecharge", FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode() );
        }

        if("after".equals(fishCardFilterParam.getRechargeType())){
            if(null == fishCardFilterParam.getRechargeValue()) {
                query.setParameter("statusRecharge", FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode() );
            }else {
                query.setParameter("statusRechargeValue", fishCardFilterParam.getRechargeValue());
            }
        }


//        if(null!=fishCardFilterParam.getTeacherName()){
//            query.setParameter("teacherName",fishCardFilterParam.getTeacherName());
//        }
        query.setParameter("orderId",Long.MAX_VALUE);
        return query;
    }




}