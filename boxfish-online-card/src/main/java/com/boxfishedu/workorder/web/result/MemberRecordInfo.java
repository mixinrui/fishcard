package com.boxfishedu.workorder.web.result;

import lombok.Data;

import java.util.Objects;

/**
 * Created by hucl on 17/4/7.
 */
@Data
public class MemberRecordInfo {
    private Integer commentUsed;
    private Integer smallClassUsed;
    private Integer publicClassUsed;
    private Integer one2OneChineseUsed;
    private Integer one2OneForeignUsed;

    public MemberRecordInfo addCommentUsed(Integer commentUsed) {
        this.commentUsed = commentUsed;
        return this;
    }

    public MemberRecordInfo addSmallClassUsed(Integer smallClassUsed) {
        this.smallClassUsed = smallClassUsed;
        return this;
    }

    public MemberRecordInfo addPublicClassUsed(Integer publicClassUsed) {
        this.publicClassUsed = publicClassUsed;
        return this;
    }

    public MemberRecordInfo addOne2OneChineseUsed(Integer one2OneChineseUsed) {
        this.one2OneChineseUsed = one2OneChineseUsed;
        return this;
    }

    public MemberRecordInfo addOne2OneForeignUsed(Integer one2OneForeignUsed) {
        this.one2OneForeignUsed = one2OneForeignUsed;
        return this;
    }

    public void dealNull() {
        if (Objects.isNull(commentUsed)) {
            commentUsed = 0;
        }
        if (Objects.isNull(smallClassUsed)) {
            smallClassUsed = 0;
        }
        if (Objects.isNull(publicClassUsed)) {
            publicClassUsed = 0;
        }
        if (Objects.isNull(one2OneChineseUsed)) {
            one2OneChineseUsed = 0;
        }
        if (Objects.isNull(one2OneForeignUsed)) {
            one2OneForeignUsed = 0;
        }
    }

    public static MemberRecordInfo emptyMemberRecordInfo() {
        return new MemberRecordInfo()
                .addCommentUsed(0)
                .addOne2OneForeignUsed(0)
                .addPublicClassUsed(0)
                .addOne2OneChineseUsed(0)
                .addSmallClassUsed(0);
    }
}
