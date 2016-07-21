package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by hucl on 16/7/20.
 */
@Service
public class CommentCardTeacherAppService {

    @Autowired
    private CommentCardJpaRepository commentCardJpaRepository;

    public Page<CommentCard> findByTeacherIdOrderByAssignTeacherTimeDesc(Long teacherId, Pageable pageable){
        return commentCardJpaRepository.findByTeacherIdOrderByAssignTeacherTimeDesc(teacherId,pageable);
    }

    public CommentCard findById(Long id){
        return commentCardJpaRepository.findOne(id);
    }

    public void save(CommentCard commentCard){
        this.save(commentCard);
    }

}
