package com.boxfishedu.card.comment.manage.entity.enums;

import java.time.LocalDateTime;

/**
 * Created by LuoLiBing on 16/9/5.
 */
public enum NotAnswerTime {
    _24HOURS(0),_24_36HOURS(1), _36HOURS(2), ;

    private int code;

    NotAnswerTime(int code) {
        this.code = code;
    }

    public DateRange getRange() {
        LocalDateTime now = LocalDateTime.now();
        switch (code) {
            case 0: return new DateRange(now.minusHours(24), now);
            case 1: return new DateRange(now.minusHours(36), now.minusHours(24));
            case 2: return new DateRange(now.minusHours(48), now.minusHours(36));
            default: throw new IllegalArgumentException("参数应该在(0,1,2)当中");
        }
    }

    public static NotAnswerTime resolve(int code) {
        for (NotAnswerTime notAnswer: NotAnswerTime.values()){
            if (notAnswer.code == code)
                return notAnswer;
        }
        return null;
    }

    public class DateRange {
        private LocalDateTime from;
        private LocalDateTime to;

        public DateRange() {
        }

        public DateRange(LocalDateTime from, LocalDateTime to) {
            this.from = from;
            this.to = to;
        }

        public LocalDateTime getFrom() {
            return from;
        }

        public void setFrom(LocalDateTime from) {
            this.from = from;
        }

        public LocalDateTime getTo() {
            return to;
        }

        public void setTo(LocalDateTime to) {
            this.to = to;
        }
    }
}
