package com.boxfishedu.workorder.servicex.instantclass.instantvalidator;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.mongo.InstantClassTimeRulesMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.servicex.instantclass.container.ThreadLocalUtil;
import com.boxfishedu.workorder.web.param.InstantRequestParam;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by hucl on 16/11/4.
 */
@Order(1)
@Component
public class TimeRangeValidator implements InstantClassValidator {
    @Autowired
    private InstantClassTimeRulesMorphiaRepository instantClassTimeRulesMorphiaRepository;

    @Override
    public int preValidate() {
        Date date=new Date();
        String day=DateUtil.date2SimpleString(date);
        InstantRequestParam instantRequestParam = ThreadLocalUtil.instantRequestParamThreadLocal.get();

        Optional<List<InstantClassTimeRules>> instantClassTimeRulesList
                =instantClassTimeRulesMorphiaRepository.getByDay(day,instantRequestParam.getTutorType());

        if(instantClassTimeRulesList.isPresent()){
            long inRange=instantClassTimeRulesList.get().stream().map(
                    instantClassTimeRules -> new DateRange(
                            DateUtil.String2Date(String.join(" ",day,instantClassTimeRules.getBegin()))
                            ,DateUtil.String2Date(String.join(" ",day,instantClassTimeRules.getEnd()))))
                            .filter(dateRange -> dateRange.inRange(date)).count();
            if(inRange==0){
                return InstantClassRequestStatus.NOT_IN_RANGE.getCode();
            }
        }
        return InstantClassRequestStatus.UNKNOWN.getCode();
    }

    @Data
    class DateRange {
        private Date from;
        private Date end;

        public boolean inRange(Date date){
            return date.before(end)&&date.after(from);
        }
        public DateRange(Date from,Date end){
            this.from=from;
            this.end=end;
        }
    }
}
