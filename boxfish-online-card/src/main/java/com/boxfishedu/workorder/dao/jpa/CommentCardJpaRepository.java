package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface CommentCardJpaRepository extends JpaRepository<CommentCard, Long>, CommentCardJpaRepositoryCustom{

    public Page<CommentCard> findByTeacherIdOrderByAssignTeacherTimeDesc(Long teacherId, Pageable pageable);

    public CommentCard findByIdAndStudentId(Long id,Long studentId);

    public CommentCard findByIdAndTeacherIdAndStatus(Long id, Long teacherId, Integer status);

    @Query("select count(c) as count from CommentCard c where c.studentId=?1 and c.studentReadFlag = 0")
    public long countStudentUnreadCommentCards(Long studentId);
}
