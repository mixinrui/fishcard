package com.boxfishedu.workorder.servicex.commentcard;

import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;

/**
 * Created by hucl on 16/7/20.
 */
@Service
public class StudentComment2TeacherServiceX {

    CommentCardTeacherAppService commentCardTeacherAppService;

    public void studentComment2Teacher(Student2TeacherCommentParam student2TeacherCommentParam){

        CommentCardTeacherAppService commentCardTeacherAppService;
    }
}
