package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * Created by ansel on 16/11/12.
 */
@Data
public class CommentCardStarForm {

    private Long commentCardId;

    private Long studentId;

    private Integer starLevel;
}
