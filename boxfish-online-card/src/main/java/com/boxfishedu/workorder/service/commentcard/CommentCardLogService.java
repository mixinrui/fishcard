package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.threadpool.LogPoolManager;
import com.boxfishedu.workorder.dao.mongo.CommentCardLogMorphiaRepository;
import com.boxfishedu.workorder.dao.mongo.WorkOrderLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.CommentCardLog;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by hucl on 16/4/13.
 */
@Component
public class CommentCardLogService {
    @Autowired
    private LogPoolManager logPoolManager;

    @Autowired
    private CommentCardLogMorphiaRepository commentCardLogMorphiaRepository;

    public void  save(CommentCardLog commentCardLog){
        commentCardLogMorphiaRepository.save(commentCardLog);
    }

    public void  save(Iterable<CommentCardLog> commentCardLogs){
        commentCardLogMorphiaRepository.save(commentCardLogs);
    }

    public List<CommentCardLog> queryByCommentCardId(Long commentCardId) {
        return commentCardLogMorphiaRepository.queryByCommentCardId(commentCardId);
    }

    public void batchSaveCommentCardLogs(List<CommentCardLog> commentCardLogs){
        //批量生成鱼卡流水日志,此处不用关心其是否执行完,将其放入线程池提高效率
        logPoolManager.execute(new Thread(() -> {
            this.save(commentCardLogs);
        }));
    }

    public void saveCommentCardLog(CommentCard commentCard){
    }

    public void saveCommentCardLog(CommentCard commentCard,String desc){

    }

    private void saveCommentCards(List<CommentCard> commentCards) {
        List<CommentCardLog> commentCardLogs = new ArrayList<>();
        for (CommentCard commentCard : commentCards) {
            CommentCardLog commentCardLog = new CommentCardLog();

            commentCardLogs.add(commentCardLog);
        }
        save(commentCardLogs);
    }
}
