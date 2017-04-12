package com.boxfishedu.workorder.servicex.member;

import com.boxfishedu.mall.enums.ProductType;
import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.PublicClassInfoJpaRepository;
import com.boxfishedu.workorder.web.result.MemberRecordInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by hucl on 17/4/7.
 */
@Service
public class MemberServiceX {

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private PublicClassInfoJpaRepository publicClassInfoJpaRepository;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public MemberRecordInfo memberRecord(Long studentId) {
        try {
            //外教点评
            Integer usedComment = serviceJpaRepository.usedCommentAmount(studentId, ProductType.COMMENT.value());

            //小班课
            Integer smallUsed = workOrderJpaRepository
                    .multiUsedAmount(studentId, ClassTypeEnum.SMALL.name(), FishCardStatusEnum.TEACHER_ASSIGNED.getCode()
                            , FishCardStatusEnum.TEACHER_ABSENT.getCode(), FishCardStatusEnum.STUDENT_ABSENT.getCode());

            //公开课
            Integer usedPublic = publicClassInfoJpaRepository.usedPublicCount(studentId);

            //中教1对1
            Integer usedChineseSingle = workOrderJpaRepository
                    .singleUsedAmount(studentId, ClassTypeEnum.SMALL.name()
                            , TeachingType.ZHONGJIAO.getCode(), FishCardStatusEnum.TEACHER_ASSIGNED.getCode()
                            , FishCardStatusEnum.TEACHER_ABSENT.getCode(), FishCardStatusEnum.STUDENT_ABSENT.getCode());

            //外教1对1
            Integer usedForeignSingle = workOrderJpaRepository
                    .singleUsedAmount(studentId, ClassTypeEnum.SMALL.name()
                            , TeachingType.WAIJIAO.getCode(), FishCardStatusEnum.TEACHER_ASSIGNED.getCode()
                            , FishCardStatusEnum.TEACHER_ABSENT.getCode(), FishCardStatusEnum.STUDENT_ABSENT.getCode());

            MemberRecordInfo memberRecordInfo = new MemberRecordInfo();
            memberRecordInfo.addCommentUsed(usedComment)
                    .addOne2OneChineseUsed(usedChineseSingle)
                    .addOne2OneForeignUsed(usedForeignSingle)
                    .addSmallClassUsed(smallUsed)
                    .addPublicClassUsed(usedPublic);

            memberRecordInfo.dealNull();

            return memberRecordInfo;
        } catch (Exception ex) {
            logger.error("获取剩余信息失败,学生[{}]", studentId, ex);
            return MemberRecordInfo.emptyMemberRecordInfo();
        }
    }
}
