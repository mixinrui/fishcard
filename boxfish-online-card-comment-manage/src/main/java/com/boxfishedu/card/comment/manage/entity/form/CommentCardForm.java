package com.boxfishedu.card.comment.manage.entity.form;

import com.boxfishedu.card.comment.manage.entity.enums.NotAnswerTime;
import lombok.Data;

import java.util.Date;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Data
public class CommentCardForm {

    /**
     * 点评创建时间范围
     */
    private NotAnswerTime notAnswerTime;

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
    private DateRange studentAskTimeRange;

    /**
     * 订单类型
     */
    private String orderChannel;

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

