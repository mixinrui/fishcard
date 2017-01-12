package com.boxfishedu.workorder.common.bean;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.boxfishedu.workorder.common.bean.CourseDifficultyEnum.*;

/**
 * Created by LuoLiBing on 17/1/9.
 * 上课时间设置枚举类
 * 学生难度为level1, 2, 只显示19:00, 上level2的课程
 * 学生难度为level3, 只显示19:30, 上level3的课程
 * 学生难度为level4, 只显示20:00, 上level4的课程
 * 学生难度为level5|6|7|8, 只显示20:30, 上level5的课课程
 *
 */
public enum PublicClassTimeEnum {

    // 19:00
    NINETEEN_1(LEVEL1, LEVEL2) {
        @Override
        public TimeRange getTimeRange() {
            return new TimeRange(25, LocalTime.of(19, 0, 0), LocalTime.of(19, 30, 0));
        }
    },

    // 19:30
    NINETEEN_2(LEVEL3) {
        @Override
        public TimeRange getTimeRange() {
            return new TimeRange(26, LocalTime.of(19,30, 0), LocalTime.of(20, 0, 0));
        }
    },

    // 20:00
    TWENTY_1(LEVEL4) {
        @Override
        public TimeRange getTimeRange() {
            return new TimeRange(27, LocalTime.of(20,0, 0), LocalTime.of(20, 30, 0));
        }
    },

    // 20:30
    TWENTY_2(LEVEL5, LEVEL6, LEVEL7, LEVEl8) {
        @Override
        public TimeRange getTimeRange() {
            return new TimeRange(28, LocalTime.of(20,30, 0), LocalTime.of(21, 0, 0));
        }
    };

    private CourseDifficultyEnum[] difficulties;

    PublicClassTimeEnum(CourseDifficultyEnum...difficulties) {
        this.difficulties = difficulties;
    }

    private static EnumMap<CourseDifficultyEnum, PublicClassTimeEnum> publicClassTimes
            = new EnumMap<>(CourseDifficultyEnum.class);

    private static Map<Integer, Set<CourseDifficultyEnum>> slotIdPublicClassTimes = new HashMap<>();

    static {
        for(PublicClassTimeEnum val : values()) {
            for(CourseDifficultyEnum key : val.difficulties) {
                publicClassTimes.put(key, val);
                slotIdPublicClassTimes.compute(val.getTimeRange().getSlotId(), (k, v) -> {
                    if(v == null) {
                       v = new HashSet<>();
                    }
                    v.add(key);
                    return v;
                });
                slotIdPublicClassTimes.putIfAbsent(val.getTimeRange().getSlotId(), new HashSet<>()).add(key);
            }
        }
    }

    public static PublicClassTimeEnum publicClassTime(CourseDifficultyEnum courseDifficulty) {
        return publicClassTimes.get(courseDifficulty);
    }

    public static Set<CourseDifficultyEnum> getCourseDifficultiesBySlotId(Integer slotId) {
        return slotIdPublicClassTimes.get(slotId);
    }

    public TimeRange getTimeRange() {
        throw new BusinessException("不支持的上课时间");
    }

    /**
     * 上课时间范围
     */
    public class TimeRange implements Serializable {
        private static final long serialVersionUID = 1L;

        public final Integer slotId;
        public final LocalTime from, to;
        private LocalDate classDate;

        public TimeRange(Integer slotId, LocalTime from, LocalTime to) {
            this.slotId = slotId;
            this.from = from;
            this.to = to;
        }

        public TimeRange setClassDate(LocalDate classDate) {
            this.classDate = classDate;
            return this;
        }

        public String getFrom() {
            return DateUtil.timeFormatter1.format(from);
        }

        public String getTo() {
            return DateUtil.timeFormatter1.format(to);
        }

        public Integer getSlotId() {
            return slotId;
        }

        public String getFromDate() {
            return classDate == null? null: DateUtil.formatLocalDateTime(DateUtil.merge(classDate, from));
        }

        public String getToDate() {
            return classDate == null? null: DateUtil.formatLocalDateTime(DateUtil.merge(classDate, to));
        }
    }
}
