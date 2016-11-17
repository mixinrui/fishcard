package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.dao.jpa.CommentCardStarJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCardStar;
import com.boxfishedu.workorder.entity.mysql.CommentCardStarForm;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Created by ansel on 16/11/12.
 */
@Service
public class CommentCardStarTeacherAppService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CommentCardStarJpaRepository commentCardStarJpaRepository;

    @Autowired
    private CommentCardSDK commentCardSDK;

    public CommentCardStar saveTeacher2StudentStar(CommentCardStarForm commentCardStarForm, Long teacherId, String accessToken){
        CommentCardStar commentCardStar = new CommentCardStar(commentCardStarForm.getCommentCardId(),
                commentCardStarForm.getStudentId(),teacherId,commentCardStarForm.getStarLevel());
        logger.info("###saveTeacher2StudentStar1### 保存学生获得的星级和积分 {}", commentCardStar);
        CommentCardStar newCommentCardStar = commentCardStarJpaRepository.save(commentCardStar);

        Object object = commentCardSDK.addScore2Student(commentCardStarForm.getStudentId(),accessToken,newCommentCardStar.getPoint());
        logger.info("###saveTeacher2StudentStar2### 学生增加积分情况 {}", object);
        return newCommentCardStar;
    }

    public JsonResultModel showStarInfo(CommentCardStar commentCardStar){
        JsonResultModel jsonResultModel;
        if (Objects.isNull(commentCardStar.getId())){
            jsonResultModel = new JsonResultModel();
            jsonResultModel.setData("对学生评星失败!");
            jsonResultModel.setReturnCode(500);
            jsonResultModel.setReturnMsg("Failed!");
        }else {
            jsonResultModel = new JsonResultModel();
            jsonResultModel.setData("对学生评星成功!");
            jsonResultModel.setReturnCode(200);
            jsonResultModel.setReturnMsg("Succeed!");
        }

        return jsonResultModel;
    }
}
