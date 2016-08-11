package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.dao.jpa.CommentCardUnanswerTeacherJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Created by hucl on 16/7/20.
 */
@Service
public class CommentCardTeacherAppService {

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

    public Page<CommentCard> queryTeacherAnswerList(Pageable pageable, Long teacherId){
        return commentCardJpaRepository.queryTeacherAnsweredList(pageable,teacherId);
    }

    public Page<CommentCard> queryTeacherUnanswerList(Pageable pageable, Long teacherId){
        return commentCardJpaRepository.queryTeacherUnAnsweredList(pageable,teacherId);
    }

    public CommentCard checkTeacher(Long id, Long teacherId){
        return commentCardJpaRepository.findByIdAndTeacherIdAndStatus(id, teacherId,300);
    }
}
