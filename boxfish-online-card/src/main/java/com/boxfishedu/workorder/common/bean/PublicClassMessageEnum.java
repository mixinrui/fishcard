package com.boxfishedu.workorder.common.bean;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LuoLiBing on 17/1/10.
 */
public enum PublicClassMessageEnum {

    SUCCES {
        @Override
        public char getCode() {
            return 'Y';
        }
    },

    // 老板表示非会员,新版表示不是公开课用户
    NON_MEMBER {
        @Override
        public String getTitle() {
            return "外教大讲堂";
        }

        @Override
        public String getMessage() {
            return "先购买后上课~ 49.8元/月，更可获赠相同时长的学员全服务！";
        }

        @Override
        public char getCode() {
            return 'M';
        }
    },

    // 每天上课限制
    EVERY_DAY_LIMIT() {
        @Override
        public String getTitle() {
            return "每天一次";
        }

        @Override
        public String getMessage() {
            return "每天可以上一次外教公开课; 与自主学习搭配会效果更好呦. 去自学一课吧~";
        }
    },

    // 未到正常上课时间
    ERROR_TIME_BEFORE {
        @Override
        public String getTitle() {
            return "未到上课时间";
        }

        @Override
        public String getMessage() {
            return "同学, 你很积极呦, 但是上课时间还没到呢, 准点再来吧~";
        }
    },

    // 超过正常上课时间
    ERROR_TIME_AFTER {
        @Override
        public String getTitle() {
            return "课程已结束";
        }

        @Override
        public String getMessage() {
            return "啊，同学~ 你已经错过今天上课的时间啦，请明天准时再来吧！";
        }
    },

    // 错误的公开课
    ERROR_PUBLIC_CLASS {
        @Override
        public String getTitle() {
            return "温馨提醒";
        }

        @Override
        public String getMessage() {
            return "未安排的公开课~";
        }
    },

    // 其他错误, 网络错误
    NETWORK_ERROR {
        @Override
        public String getTitle() {
            return "温馨提示";
        }

        @Override
        public String getMessage() {
            return "网络出了点问题, 请稍后重试";
        }
    };

    public char getCode() {
        return 'N';
    }

    public String getTitle() {
        return null;
    }

    public String getMessage() {
        return null;
    }

    public Map<String, Object> getMessageMap() {
        return enumMap.get(this);
    }

    private final static EnumMap<PublicClassMessageEnum, Map<String, Object>> enumMap
            = new EnumMap<>(PublicClassMessageEnum.class);

    static  {
        for(PublicClassMessageEnum publicClassMessage : values()) {
            Map<String, Object> beanMap = new HashMap<>();
            beanMap.put("title", publicClassMessage.getTitle());
            beanMap.put("message", publicClassMessage.getMessage());
            beanMap.put("code", publicClassMessage.getCode());
            enumMap.put(publicClassMessage, Collections.unmodifiableMap(beanMap));
        }
    }
}
