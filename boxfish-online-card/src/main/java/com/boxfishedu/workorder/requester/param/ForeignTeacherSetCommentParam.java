package com.boxfishedu.workorder.requester.param;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by hucl on 16/7/25.
 */
@Data
public class ForeignTeacherSetCommentParam {
    private Long teacherId;
    //点评卡id
    private Long fishCardId;

    private List<String> forGoodReviews;

    private List<String> forBadReviews;

    public static ForeignTeacherSetCommentParam paramAdapter(Student2TeacherCommentParam student2TeacherCommentParam, CommentCard commentCard){
        ForeignTeacherSetCommentParam foreignTeacherSetCommentParam=new ForeignTeacherSetCommentParam();
        foreignTeacherSetCommentParam.setFishCardId(student2TeacherCommentParam.getCommentCardId());
        foreignTeacherSetCommentParam.setTeacherId(commentCard.getTeacherId());

        if(null!=student2TeacherCommentParam.getForGoodReviews()) {
            List<String> forGoodReviews = Lists.newArrayList();
            forGoodReviews.add(student2TeacherCommentParam.getForGoodReviews());
            foreignTeacherSetCommentParam.setForGoodReviews(forGoodReviews);
        }

        if(null!=student2TeacherCommentParam.getForBadReviews()) {
            List<String> forBadReviews = Lists.newArrayList();
            forBadReviews.add(student2TeacherCommentParam.getForBadReviews());
            foreignTeacherSetCommentParam.setForBadReviews(forBadReviews);
        }

        return foreignTeacherSetCommentParam;
    }
}
