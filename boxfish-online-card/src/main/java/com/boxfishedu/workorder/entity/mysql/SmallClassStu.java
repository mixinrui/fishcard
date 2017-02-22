package com.boxfishedu.workorder.entity.mysql;

import com.boxfishedu.workorder.common.bean.FishCardNetStatusEnum;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 小班课补课学生列表
 */
@Component
@Data
@Entity
@Table(name = "small_class_stu")
public class SmallClassStu implements Cloneable {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "student_id", nullable = true)
    private Long studentId;

    @Column(name = "student_name", nullable = true, length = 20)
    private String studentName;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Transient
    private String level; //学生难度

}
