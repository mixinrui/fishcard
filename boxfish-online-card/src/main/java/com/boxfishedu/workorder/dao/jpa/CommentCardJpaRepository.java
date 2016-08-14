package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;


public interface CommentCardJpaRepository extends JpaRepository<CommentCard, Long>, CommentCardJpaRepositoryCustom{

    public Page<CommentCard> findByTeacherIdOrderByAssignTeacherTimeDesc(Long teacherId, Pageable pageable);

    public CommentCard findByIdAndStudentId(Long id,Long studentId);

    public CommentCard findByIdAndTeacherIdAndStatus(Long id, Long teacherId, Integer status);

    @Query("select count(c) as count from CommentCard c where c.studentId=?1 and c.studentReadFlag = 0")
    public long countStudentUnreadCommentCards(Long studentId);

    @Query("select count(c) as count from CommentCard c where c.teacherId=?1 and c.teacherReadFlag = 0")
    public long countTeacherUnreadCommentCards(Long teacherId);

    @Query("update CommentCard c set c.studentPicturePath =?1 where c.studentId = ?2")
    public int updateStudentPicture(String studentPicturePath, Long studentId);

    @Query("update CommentCard c set c.teacherPicturePath =?1 where c.teacherId = ?2")
    public int updateTeacherPicture(String teacherPicturePath, Long teacherId);

    /**
     * 提问超过24小时还未回答
     * @param from
     * @param to
     * @param status
     * @return
     */
    @Query("select c from CommentCard c where c.studentAskTime between ?1 and ?2 and c.status<=?3")
    List<CommentCard> findByDateRangeAndStatus(Date from, Date to, Integer status);

    @Query("select c from CommentCard c where c.studentAskTime between ?1 and ?2 and c.status=?3")
    List<CommentCard> findByDateRangeAndStatus2(Date from, Date to, Integer status);
}
