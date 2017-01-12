package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.mall.enums.OrderChannelDesc;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * 公开课查询
 * Created by jiaozijun on 17/1/11.
 */

@Component
public class SmallClassQueryService {

    @Autowired
    private EntityManager entityManager;

    public Long filterFishCardsCount(PublicFilterParam publicFilterParam) {
        String prefix = "select count(wo) ";
        String sql = prefix + getFilterSql(publicFilterParam);
        Query query = getFilterQuery(sql, publicFilterParam, entityManager);
        Long count = (Long) query.getSingleResult();
        return count;
    }


    public List<SmallClass> filterFishCards(PublicFilterParam publicFilterParam, Pageable pageable) {
        String prefix = "select wo ";
        String sql = prefix + getFilterSql(publicFilterParam);
        Query query = getFilterQuery(sql, publicFilterParam, entityManager);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<SmallClass> smallClasss = query.getResultList();
        return smallClasss;
    }


    private String getFilterSql(PublicFilterParam publicFilterParam) {

        StringBuilder sql = new StringBuilder("from SmallClass wo where wo.startTime between :begin and :end ");

        if (null != publicFilterParam.getOrderType()) {
            if (publicFilterParam.getOrderType().equals(OrderChannelDesc.OVERALL.getCode())
                    ||
                    publicFilterParam.getOrderType().equals(OrderChannelDesc.FOREIGN.getCode())
                    ) {
                sql.append(" and wo.comboType=:orderChannel and wo.orderChannel= '").append(OrderChannelDesc.STANDARD.getCode()).append("' ");
            } else if (publicFilterParam.getOrderType().equals(OrderChannelDesc.CHINESE.getCode())) {       // 终极梦想
                sql.append(" and (wo.comboType=:orderChannel or ( wo.comboType= '").append(OrderChannelDesc.INTELLIGENT.getCode()).append("' ").
                        append(" and  wo.service.tutorType= '").append(TutorTypeEnum.FRN).append("' )  )  and wo.orderChannel= '").append(OrderChannelDesc.STANDARD.getCode()).append("'");
            } else if (publicFilterParam.getOrderType().equals(OrderChannelDesc.INTELLIGENT.getCode())) { // 考试指导
                sql.append(" and wo.comboType=:orderChannel  ").append(" and  wo.service.tutorType= '").append(TutorTypeEnum.CN).append("'   and wo.orderChannel= '").append(OrderChannelDesc.STANDARD.getCode()).append("'");
            } else {
                sql.append(" and wo.orderChannel=:orderChannel ");
            }

        }
        if (null != publicFilterParam.getConfirmFlag()) {
            if ("1".equals(publicFilterParam.getConfirmFlag())) {
                sql.append(" and (wo.confirmFlag=:confirmFlag or wo.confirmFlag is null )  ");
            } else {
                sql.append(" and wo.confirmFlag=:confirmFlag ");
            }
        }

        if ("before".equals(publicFilterParam.getRechargeType())) {
            sql.append(" and wo.statusRecharge = :statusRecharge ");  //
        }

        if ("after".equals(publicFilterParam.getRechargeType())) {
            if (null == publicFilterParam.getRechargeValue()) {
                sql.append(" and wo.statusRecharge > :statusRecharge ");  //
            } else {
                sql.append(" and wo.statusRecharge = :statusRechargeValue ");  //
            }
        }

        // 中外教
        if (null != publicFilterParam.getTeachingType()) {
            sql.append(" and wo.skuId = :teachingType ");
        }

        if (null != publicFilterParam.getCreateBeginDateFormat()) {
            sql.append(" and wo.createTime>=:createbegin ");
        }
        if (null != publicFilterParam.getCreateEndDateFormat()) {
            sql.append(" and wo.createTime<=:createend ");
        }

//        if(null!=publicFilterParam.getStatus()){
//            sql.append("and status in (:status )");
//        }
        if (null != publicFilterParam.getOrderCode()) {
            sql.append("and orderCode=:orderCode ");
        }
        if (null != publicFilterParam.getId()) {
            sql.append("and id=:id ");
        }
        if (null != publicFilterParam.getStudentId()) {
            sql.append("and studentId=:studentId ");
        }

        if (null != publicFilterParam.getTeacherId()) {
            sql.append("and teacherId=:teacherId ");
        }

        if (null != publicFilterParam.getTeacherName() && StringUtils.isNotEmpty(publicFilterParam.getTeacherName().trim())) {
            sql.append("and teacherName like '%").append(publicFilterParam.getTeacherName()).append("%' ");
        }

        if (null != publicFilterParam.getCourseType() && StringUtils.isNotEmpty(publicFilterParam.getCourseType())) {
            sql.append("and courseType in (").append(splitCourseType(publicFilterParam.getCourseType())).append(") ");
        }

        if (null != publicFilterParam.getStatuses() && StringUtils.isNotEmpty(publicFilterParam.getStatuses())) {
            sql.append("and status in (").append(splitCourseTypeString(publicFilterParam.getStatuses())).append(") ");
        }



//        if (StringUtils.isNotEmpty(publicFilterParam.getDemoType())) {
//            if (publicFilterParam.getDemoType().trim().equals("true")) {
//                sql.append("and orderId=:orderId ");
//            } else {
//                sql.append("and orderId !=:orderId ");
//            }
//        } else {
//            sql.append("and orderId !=:orderId ");
//        }


        if (null != publicFilterParam.getMakeUpFlag()) {
            if (publicFilterParam.getMakeUpFlag()) {
                sql.append(" and parentId is not null ");
            } else {
                sql.append(" and parentId is  null  ");
            }
        }

        //小班课 公开课的 处理
        if(null != publicFilterParam.getClassType()){
            sql.append(" and classType =:classType ");  // 除了小班课 和公开课
        }

        if (null != publicFilterParam.getStartTimeSort()) {
            sql.append("order by wo.startTime   ").append(publicFilterParam.getStartTimeSort().toLowerCase());
        }

        if (null != publicFilterParam.getActualStartTimeSort()) {
            sql.append("order by wo.actualStartTime ").append(publicFilterParam.getActualStartTimeSort());
        }

        if (null != publicFilterParam.getStartTimeSort() && null != publicFilterParam.getActualStartTimeSort()) {
            sql.append("order by wo.teacherId asc , wo.createTime desc");
        }


        if (null != publicFilterParam.getTeacherNameSort()) {
            sql.append(" order by teacherName ").append(publicFilterParam.getTeacherNameSort());
        }


        return sql.toString();
    }


    private Query getFilterQuery(String sql, PublicFilterParam publicFilterParam, EntityManager entityManager) {
        Query query = entityManager.createQuery(sql);
        query.setParameter("begin", publicFilterParam.getBeginDateFormat());
        query.setParameter("end", publicFilterParam.getEndDateFormat());

        if (null != publicFilterParam.getId()) {
            query.setParameter("id", publicFilterParam.getId());
        }
        
        if (null != publicFilterParam.getOrderType()) {
            query.setParameter("classType", publicFilterParam.getClassType());
        }


        // 订单类型
        if (null != publicFilterParam.getOrderType()) {
            query.setParameter("orderChannel", publicFilterParam.getOrderType());
        }
        if (null != publicFilterParam.getContineAbsenceNum()) {
            query.setParameter("comboType", ComboTypeEnum.EXCHANGE.toString());
        }

        if (null != publicFilterParam.getCreateBeginDateFormat()) {
            query.setParameter("createbegin", publicFilterParam.getCreateBeginDateFormat());
        }
        if (null != publicFilterParam.getCreateEndDateFormat()) {
            query.setParameter("createend", publicFilterParam.getCreateEndDateFormat());
        }

//        if(null!=publicFilterParam.getStatus()){
//            query.setParameter("status",publicFilterParam.getStatus());
//        }
        if (null != publicFilterParam.getOrderCode()) {
            query.setParameter("orderCode", publicFilterParam.getOrderCode());
        }
        if (null != publicFilterParam.getStudentId()) {
            query.setParameter("studentId", publicFilterParam.getStudentId());
        }
        if (null != publicFilterParam.getTeacherId()) {
            query.setParameter("teacherId", publicFilterParam.getTeacherId());
        }

        if (null != publicFilterParam.getConfirmFlag()) {
            query.setParameter("confirmFlag", publicFilterParam.getConfirmFlag());
        }

        if ("before".equals(publicFilterParam.getRechargeType())) {
            query.setParameter("statusRecharge", FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode());
        }

        if ("after".equals(publicFilterParam.getRechargeType())) {
            if (null == publicFilterParam.getRechargeValue()) {
                query.setParameter("statusRecharge", FishCardChargebackStatusEnum.NEED_RECHARGEBACK.getCode());
            } else {
                query.setParameter("statusRechargeValue", publicFilterParam.getRechargeValue());
            }
        }

        if (null != publicFilterParam.getTeachingType()) {

            query.setParameter("teachingType", publicFilterParam.getTeachingType());
        }

        //小班课 公开课的 处理
        if( null != publicFilterParam.getClassType()){
            query.setParameter("classType", publicFilterParam.getClassType());
        }
        return query;
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


}
