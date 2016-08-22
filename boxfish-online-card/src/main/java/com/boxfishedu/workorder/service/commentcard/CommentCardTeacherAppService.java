package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.dao.jpa.CommentCardUnanswerTeacherJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hucl on 16/7/20.
 */
@Service
public class CommentCardTeacherAppService {

    private Logger logger = LoggerFactory.getLogger(CommentCardTeacherAppService.class);

    @Autowired
    private CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    private CommentCardUnanswerTeacherJpaRepository commentCardUnanswerTeacherJpaRepository;

    public Page<CommentCard> findByTeacherIdOrderByAssignTeacherTimeDesc(Long teacherId, Pageable pageable){
        return commentCardJpaRepository.findByTeacherIdOrderByAssignTeacherTimeDesc(teacherId,pageable);
    }

    public CommentCard findById(Long id){
        return commentCardJpaRepository.findOne(id);
    }

    public CommentCard save(CommentCard commentCard){
        return commentCardJpaRepository.save(commentCard);
    }

    public Map queryTeacherAnswerList(Pageable pageable, Long teacherId){
        Page<CommentCard> commentCardPage = commentCardJpaRepository.queryTeacherAnsweredList(pageable,teacherId);
        Map commentCardsMap = new LinkedHashMap<>();
        commentCardsMap.put("content",commentCardPage.getContent());
        commentCardsMap.put("totalPages",commentCardPage.getTotalPages());
        commentCardsMap.put("number",commentCardPage.getNumber());
        commentCardsMap.put("totalElements",commentCardPage.getTotalElements());
        commentCardsMap.put("unreadTotalElements",countTeacherDoneUnread(teacherId));
        return commentCardsMap;
    }

    public Map queryTeacherUnanswerList(Pageable pageable, Long teacherId){
        Page<CommentCard> commentCardPage = commentCardJpaRepository.queryTeacherUnAnsweredList(pageable,teacherId);
        Map commentCardsMap = new LinkedHashMap<>();
        commentCardsMap.put("content",commentCardPage.getContent());
        commentCardsMap.put("totalPages",commentCardPage.getTotalPages());
        commentCardsMap.put("number",commentCardPage.getNumber());
        commentCardsMap.put("totalElements",commentCardPage.getTotalElements());
        commentCardsMap.put("unreadTotalElements",countTeacherTodoUnread(teacherId));
        return commentCardsMap;
    }

    public CommentCard checkTeacher(Long id, Long teacherId){
        return commentCardJpaRepository.findByIdAndTeacherIdAndStatus(id, teacherId,300);
    }

    public long countTeacherDoneUnread(Long teacherId){
        logger.info("教师端调用查询Done列表未读点评个数,老师id为:"+teacherId);
        return commentCardJpaRepository.countTeacherDoneListUnread(teacherId);
    }

    public long countTeacherTodoUnread(Long teacherId){
        logger.info("教师端调用查询Todo列表未读点评个数,老师id为:"+teacherId);
        return commentCardJpaRepository.countTeacherTodoListUnread(teacherId);
    }
}
