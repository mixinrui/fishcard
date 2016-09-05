package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;


public interface CommentCardJpaRepository extends JpaRepository<CommentCard, Long>, CommentCardJpaRepositoryCustom{

    public Page<CommentCard> findByTeacherIdOrderByAssignTeacherTimeDesc(Long teacherId, Pageable pageable);

    public CommentCard findById(Long id);

    public CommentCard findByIdAndStudentId(Long id,Long studentId);

    public CommentCard findByIdAndTeacherIdAndStatus(Long id, Long teacherId, Integer status);

    @Query("select count(c) as count from CommentCard c where c.studentId=?1 and c.studentReadFlag = 0")
    public long countStudentUnreadCommentCards(Long studentId);

    @Query("select count(c) as count from CommentCard c where c.teacherId=?1 and c.teacherReadFlag = 0")
    public long countTeacherUnreadCommentCards(Long teacherId);

    @Modifying
    @Query("update CommentCard c set c.studentPicturePath =?1 where c.studentId = ?2")
    public void updateStudentPicture(String studentPicturePath, Long studentId);


    @Modifying
    @Query("update CommentCard c set c.teacherPicturePath =?1 where c.teacherId = ?2")
    public void updateTeacherPicture(String teacherPicturePath, Long teacherId);

    /**
     * 提问超过24小时还未回答
     * @param from
     * @param to
     * @param status
     * @return
    */
    @Query("select c from CommentCard c where c.studentAskTime between ?1 and ?2 and c.status<=?3 and c.assignTeacherCount = 1")
    List<CommentCard> findByDateRangeAndStatus(Date from, Date to, Integer status);

    /**
     * 提问超过48小时还未回答
     * @param from
     * @param to
     * @param status
     * @return
     */
    @Query("select c from CommentCard c where c.studentAskTime between ?1 and ?2 and c.status<=?3 and c.assignTeacherCount = 2")
    List<CommentCard> findByDateRangeAndStatus2(Date from, Date to, Integer status);

    /**
     * 未分配到老师
     */
    @Query("select c from CommentCard c where c.studentAskTime between ?1 and ?2 and c.status<=?3 and c.teacherId is null")
    List<CommentCard> findUndistributedTeacher(Date from, Date to, Integer status);

    /**
     * 统计外教点评教师端Done列表未读个数
     */
    @Query("select count(c) from CommentCard c where c.teacherId = ?1 and c.teacherReadFlag = 0 and (c.status = 400 or c.status = 500 or c.status = 600)")
    public long countTeacherDoneListUnread(Long teacherId);

    @Query("select count(c) from CommentCard c where c.teacherId = ?1 and c.teacherReadFlag = 0 and c.status = 300")
    public long countTeacherTodoListUnread(Long teacherId);

    /**
     * 外教点评强制换老师
     */
    @Modifying
    @Query("update CommentCard c set c.teacherId =?2 where c.teacherId =?1 and c.status = 300")
    public void forceToChangeTeacher(Long fromTeacherId , Long toTeacherId);
    public List<CommentCard> findByTeacherIdAndStatus(Long teacherId, Integer status);
}
