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





        // 中外教
        if (null != publicFilterParam.getRoleId()) {
            sql.append(" and wo.roleId = :roleId ");
        }

        if (null != publicFilterParam.getCreateBeginDateFormat()) {
            sql.append(" and wo.createTime>=:createbegin ");
        }
        if (null != publicFilterParam.getCreateEndDateFormat()) {
            sql.append(" and wo.createTime<=:createend ");
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

        //小班课 公开课的 处理
        if(null != publicFilterParam.getClassType()){
            sql.append(" and classType =:classType ");  // 除了小班课 和公开课
        }

        sql.append(" order by ");



        if (null != publicFilterParam.getStartTimeSort()) {
            sql.append(" wo.startTime   ").append(publicFilterParam.getStartTimeSort().toLowerCase()) .append(" ,");
        }

        if (null != publicFilterParam.getActualStartTimeSort()) {
            sql.append("  wo.actualStartTime ").append(publicFilterParam.getActualStartTimeSort())  .append(" ,");
        }

        if (null != publicFilterParam.getStartTimeSort() && null != publicFilterParam.getActualStartTimeSort()) {
            sql.append("  wo.teacherId asc , wo.createTime desc")  .append(" ,");
        }


        if (null != publicFilterParam.getTeacherNameSort()) {
            sql.append("   teacherName ").append(publicFilterParam.getTeacherNameSort());
        }

        // 按照时间倒序
        sql.append(" wo.id desc ");


        return sql.toString();
    }


    private Query getFilterQuery(String sql, PublicFilterParam publicFilterParam, EntityManager entityManager) {
        Query query = entityManager.createQuery(sql);
        query.setParameter("begin", publicFilterParam.getBeginDateFormat());
        query.setParameter("end", publicFilterParam.getEndDateFormat());

        if (null != publicFilterParam.getId()) {
            query.setParameter("id", publicFilterParam.getId());
        }
        
        if (null != publicFilterParam.getClassType()) {
            query.setParameter("classType", publicFilterParam.getClassType());
        }



        if (null != publicFilterParam.getCreateBeginDateFormat()) {
            query.setParameter("createbegin", publicFilterParam.getCreateBeginDateFormat());
        }
        if (null != publicFilterParam.getCreateEndDateFormat()) {
            query.setParameter("createend", publicFilterParam.getCreateEndDateFormat());
        }

        if(null!=publicFilterParam.getStatus()){
            query.setParameter("status",publicFilterParam.getStatus());
        }
        if (null != publicFilterParam.getOrderCode()) {
            query.setParameter("orderCode", publicFilterParam.getOrderCode());
        }
        if (null != publicFilterParam.getStudentId()) {
            query.setParameter("studentId", publicFilterParam.getStudentId());
        }
        if (null != publicFilterParam.getTeacherId()) {
            query.setParameter("teacherId", publicFilterParam.getTeacherId());
        }



        if (null != publicFilterParam.getRoleId()) {
            query.setParameter("roleId", publicFilterParam.getRoleId());
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
