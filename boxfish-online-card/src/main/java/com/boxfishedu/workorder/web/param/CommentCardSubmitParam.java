package com.boxfishedu.workorder.web.param;

import lombok.Data;

/**
 * Created by hucl on 16/7/21.
 */
@Data
public class CommentCardSubmitParam {
    private String videoPath;
    private Long answerVideoTime;
    private Long answerVideoSize;
    private Long teacherId;
    private String teacherPicturePath;
    private Long commentcardId;
}
