package com.boxfishedu.card.comment.manage.entity.form;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Data
public class CommentCardForm {

    /**
     * 点评卡id
     */
    private Long code;

    /**
     * 未回答时长范围
     */
    private Integer notAnswerTime;

    private Long teacherId;

    private String teacherName;

    /**
     * 状态
     */
    private Integer status;

    private Long studentId;

    private String studentName;

    /**
     * 学生提问时间区间
     */
//    private DateRange studentAskTimeRange;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date studentAskTimeRangeFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date studentAskTimeRangeTo;

    /**
     * 订单类型
     */
    private String orderChannel;

    private String orderCode;

    /**
     * 课程信息
     */
    private String courseType;

    private String courseDifficulty;

    @Data
    public class DateRange {
        private Date from;
        private Date to;
        public DateRange(Date from, Date to) {
            this.from = from;
            this.to = to;
        }
    }
}

