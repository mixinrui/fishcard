package com.boxfishedu.card.comment.manage.entity.form;

import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by LuoLiBing on 16/9/9.
 */
@Data
public class ChangeTeacherForm {

    private List<ReviewTagParam> escapeReviewTagParamList;

    private List<ReviewTagParam> reviewTagParamList;

    public ChangeTeacherForm() {
        this.escapeReviewTagParamList = Lists.newArrayList();
        this.reviewTagParamList = Lists.newArrayList();
    }

    @Data
    public class ReviewTagParam {
        private Long teacherId;
        private Long studentId;
        private String courseId;
        private Long fishCardId;
    }

    public void addChangeTeacher(CommentCard commentCard, Long teacherId) {
        // 释放老师资源
        ReviewTagParam escape = new ReviewTagParam();
        escape.setTeacherId(commentCard.getTeacherId());
        escape.setFishCardId(commentCard.getId());

        // 换老师
        ReviewTagParam review = new ReviewTagParam();
        review.setFishCardId(commentCard.getId());
        review.setStudentId(commentCard.getStudentId());
        review.setCourseId(commentCard.getCourseId());
        review.setTeacherId(teacherId);

        escapeReviewTagParamList.add(escape);
        reviewTagParamList.add(review);
    }
}
