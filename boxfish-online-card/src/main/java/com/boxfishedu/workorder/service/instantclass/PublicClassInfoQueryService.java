package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.mall.enums.OrderChannelDesc;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.FishCardChargebackStatusEnum;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.entity.mysql.PublicClassInfo;
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
 * Created by jiaozijun on 17/1/11.
 */
@Component
public class PublicClassInfoQueryService {

    @Autowired
    private EntityManager entityManager;


    public Long filterFishCardsCount(PublicFilterParam publicFilterParam) {
        String prefix = "select count(wo) ";
        String sql = prefix + getFilterSql(publicFilterParam);
        Query query = getFilterQuery(sql, publicFilterParam, entityManager);
        Long count = (Long) query.getSingleResult();
        return count;
    }


    public List<PublicClassInfo> filterFishCards(PublicFilterParam publicFilterParam, Pageable pageable) {
        String prefix = "select wo ";
        String sql = prefix + getFilterSql(publicFilterParam);
        Query query = getFilterQuery(sql, publicFilterParam, entityManager);
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<PublicClassInfo> smallClasss = query.getResultList();
        return smallClasss;
    }


    private String getFilterSql(PublicFilterParam publicFilterParam) {

        StringBuilder sql = new StringBuilder("from PublicClassInfo wo where wo.startTime between :begin and :end ");

        if(null!=publicFilterParam.getSmallClassId()){
            sql.append(" and wo.smallClassId = :smallClassId ");
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

        if (null != publicFilterParam.getStudentName() && StringUtils.isNotEmpty(publicFilterParam.getStudentName().trim())) {
            sql.append("and studentName like '%").append(publicFilterParam.getStudentName()).append("%' ");
        }

        if (null != publicFilterParam.getCourseType() && StringUtils.isNotEmpty(publicFilterParam.getCourseType())) {
            sql.append("and courseType in (").append(splitCourseType(publicFilterParam.getCourseType())).append(") ");
        }

        if (null != publicFilterParam.getStatuses() && StringUtils.isNotEmpty(publicFilterParam.getStatuses())) {
            sql.append("and status in (").append(splitCourseTypeString(publicFilterParam.getStatuses())).append(") ");
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

        if(null!=publicFilterParam.getSmallClassId()){
            query.setParameter("smallClassId", publicFilterParam.getSmallClassId());
        }

        if (null != publicFilterParam.getId()) {
            query.setParameter("id", publicFilterParam.getId());
        }

        if (null != publicFilterParam.getCreateBeginDateFormat()) {
            query.setParameter("createbegin", publicFilterParam.getCreateBeginDateFormat());
        }

        if (null != publicFilterParam.getCreateEndDateFormat()) {
            query.setParameter("createend", publicFilterParam.getCreateEndDateFormat());
        }


        if (null != publicFilterParam.getStudentId()) {
            query.setParameter("studentId", publicFilterParam.getStudentId());
        }
        if (null != publicFilterParam.getTeacherId()) {
            query.setParameter("teacherId", publicFilterParam.getTeacherId());
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
