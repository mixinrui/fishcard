package com.boxfishedu.card.comment.manage.entity.dto.merger;

import com.boxfishedu.card.comment.manage.config.CommentCardManageUrl;
import com.boxfishedu.card.comment.manage.util.ApplicationContextProvider;
import org.springframework.util.StringUtils;

/**
 * Created by LuoLiBing on 16/9/9.
 */
public class AliCloudPathMerger extends BaseStringMerger<String> {

    private static CommentCardManageUrl commentCardManageUrl =
            ApplicationContextProvider.getBean(CommentCardManageUrl.class);

    @Override
    public String append(String value, String[] extraParam) {
        if(StringUtils.isEmpty(value)) {
            return value;
        }
        return String.join("/", commentCardManageUrl.getAvatarsUrl(), value);
    }
}
