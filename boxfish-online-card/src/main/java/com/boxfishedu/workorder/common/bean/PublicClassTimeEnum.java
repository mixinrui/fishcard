package com.boxfishedu.workorder.common.bean;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;

import java.time.LocalTime;
import java.util.EnumMap;

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
    NINETEEN_1(LEVEL_1, LEVEL_2) {
        @Override
        public TimeRange getTimeRange() {
            return new TimeRange(25, LocalTime.of(19, 0, 0), LocalTime.of(19, 30, 0));
        }
    },

    // 19:30
    NINETEEN_2(LEVEL_3) {
        @Override
        public TimeRange getTimeRange() {
            return new TimeRange(26, LocalTime.of(19,30, 0), LocalTime.of(20, 0, 0));
        }
    },

    // 20:00
    TWENTY_1(LEVEL_4) {
        @Override
        public TimeRange getTimeRange() {
            return new TimeRange(27, LocalTime.of(20,0, 0), LocalTime.of(20, 30, 0));
        }
    },

    // 20:30
    TWENTY_2(LEVEL_5, LEVEL_6, LEVEL_7, LEVEl_8) {
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

    static {
        for(PublicClassTimeEnum val : values()) {
            for(CourseDifficultyEnum key : val.difficulties) {
                publicClassTimes.put(key, val);
            }
        }
    }

    public static PublicClassTimeEnum publicClassTime(CourseDifficultyEnum courseDifficulty) {
        return publicClassTimes.get(courseDifficulty);
    }

    public TimeRange getTimeRange() {
        throw new BusinessException("不支持的上课时间");
    }

    public Integer getSlotId() {
        throw new BusinessException("不支持的上课时间");
    }

    /**
     * 上课时间范围
     */
    public class TimeRange {
        public final Integer slotId;
        public final LocalTime from, to;
        public TimeRange(Integer slotId, LocalTime from, LocalTime to) {
            this.slotId = slotId;
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return DateUtil.formatLocalTime(from);
        }

        public String getTo() {
            return DateUtil.formatLocalTime(from);
        }

        public Integer getSlotId() {
            return slotId;
        }
    }
}
