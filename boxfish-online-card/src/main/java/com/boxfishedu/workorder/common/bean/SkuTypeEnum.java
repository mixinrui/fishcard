package com.boxfishedu.workorder.common.bean;

/**
 * Created by hucl on 16/3/19.
 */
    public enum SkuTypeEnum {
        SKU_TEACHING_ONLINE(1L), SKU_ANSWER_ONLINE(6L), SKU_COURSE_PLAN(5L),SKU_TEACHING_FOREIGNER(4L);
        private long code;
        private SkuTypeEnum(long code) {
            this.code = code;
        }

        public long value(){
            return this.code;
        }

        @Override
        public String toString() {
            return String.valueOf(this.code);
        }
    }
