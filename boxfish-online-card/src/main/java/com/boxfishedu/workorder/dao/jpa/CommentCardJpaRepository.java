package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.Service;
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

    /**
     * 查询老师id下有无该cardId的点评卡
     */
    public CommentCard findByIdAndTeacherId(Long id, Long teacherId);

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
//    @Query("select c from CommentCard c where c.studentAskTime between ?1 and ?2 and c.status<=?3")
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

    /**
     * 查询学生已点评的点评卡
     */
    @Query("select c from CommentCard c where c.studentId = ?1 and c.status in (400,600)")
    public List<CommentCard> getCommentedCard(Long studentId);

    /**
     * 查询学生未点评的点评卡
     */
    @Query("select c from CommentCard c where c.studentId = ?1 and ((c.status <= 300 or (c.studentReadFlag = 0 and c.status != 500)))")
    public List<CommentCard> getUncommentedCard(Long studentId);

    /**
     * 初始化外教点评首页列表
     */
    @Query("select distinct(studentId) from Service s where s.productType = 1002")
    public List<Long> getCommentCardHomePageList();

    /**
     * 初始获取学生首页外教点评
     */
    @Query("select c from CommentCard c where c.studentId =?1 and c.status in (400,600) and  c.teacherAnswerTime = " +
            "(select max(cd.teacherAnswerTime) from CommentCard cd where cd.studentId =?1 and cd.status in (400,600))")
    public CommentCard getHomePageCommentCard(Long studentId);

    /**
     *查询老师最新回复的点评卡T
     */
    @Query("select c from CommentCard c where c.studentId = ?1 and c.status in (400,600) and c.teacherAnswerTime =  " +
            "(select max(cd.teacherAnswerTime) from CommentCard cd where cd.studentId = ?1 and cd.status in (400,600))")
    public List<CommentCard> getTeacherNewCommentCard(Long studentId);

    /**
     * 查询学生最新提问的点评卡
     */
    @Query("select c from CommentCard c where c.studentId = ?1 and c.status <= 300 and c.studentAskTime =  " +
            "(select max(cd.studentAskTime) from CommentCard cd where cd.studentId = ?1 and cd.status <= 300)")
    public List<CommentCard> getStudentNewCommentCard(Long studentId);

    /**
     *  客服系统初始化外教点评
     */
    @Query("SELECT c FROM CommentCard c  where c.service = ?1 order by c.teacherAnswerTime DESC")
    public List<CommentCard> getSystemCommentCard(Service service);

    /**
     *  外教点评添加课程类型和难度,查询结果用于初始化类型和难度
     * @return
     */
    @Query("SELECT c FROM CommentCard c  where c.courseType is null or c.courseDifficulty is null")
    public List<CommentCard> initiateCourseTypeAndDifficulty();

    /**
     * 标记老师已读
     * @param id
     */
    @Modifying
    @Query("update CommentCard c set c.teacherReadFlag = 1  where c.id = ?1 ")
    public void markTeacherRead(Long id);

    /**
     * 标记学生已读
     * @param id
     */
    @Modifying
    @Query("update CommentCard c set c.studentReadFlag = 1  where c.id = ?1 ")
    public void markStudentRead(Long id);

    /**
     * 获取所有课程id
     */
    @Query("select distinct(c.courseId) from CommentCard c where c.status != 500")
    public List<String> getComCourseIdList();

    /**
     * 修改课程难度和类型
     */
    @Modifying
    @Query("update CommentCard c set c.courseType = ?1, c.courseDifficulty = ?2  where c.courseId = ?3 ")
    public void updateCourseTypeAndDifficulty(String type, String difficulty,String courseId);

    /**
     * 根据点评卡id和状态码查找
     */
    public CommentCard findByIdAndStatus(Long id, Integer status);
}
