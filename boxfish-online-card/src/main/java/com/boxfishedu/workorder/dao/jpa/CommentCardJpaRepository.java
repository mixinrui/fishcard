package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentCardJpaRepository extends JpaRepository<CommentCard, Long>, CommentCardJpaRepositoryCustom{

    public CommentCard findByStudentIdAndQuestionIdAndCourseId(Long studentId,Long questionId,String courseId);

    public List<CommentCard> findByStudentIdOrderByCreateTimeDesc(Long studentId);

    public CommentCard findById(Long id);

    public List<CommentCard> findByStatusBetween(int startStatus,int endStatus);

}
