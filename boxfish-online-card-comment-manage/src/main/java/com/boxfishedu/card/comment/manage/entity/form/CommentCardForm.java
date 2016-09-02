package com.boxfishedu.card.comment.manage.entity.form;

import lombok.Data;

import java.util.Date;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Data
public class CommentCardForm {

    private DateRange studentAskTimeRange;

    public class DateRange {
        private Date from;
        private Date to;

        public DateRange() {
        }

        public DateRange(Date from, Date to) {
            this.from = from;
            this.to = to;
        }

        public Date getFrom() {
            return from;
        }

        public void setFrom(Date from) {
            this.from = from;
        }

        public Date getTo() {
            return to;
        }

        public void setTo(Date to) {
            this.to = to;
        }
    }

}

