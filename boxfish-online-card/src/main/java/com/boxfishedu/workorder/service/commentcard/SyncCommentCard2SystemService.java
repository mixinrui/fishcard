package com.boxfishedu.workorder.service.commentcard;

import java.util.Date;

/**
 * Created by ansel on 16/10/11.
 */
public interface SyncCommentCard2SystemService {
    public void syncCommentCard2System(Long serviceId, int status, Date teacherAnswerTime);

    public long initializeCommentCard2System();
}
