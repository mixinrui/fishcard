package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.common.util.DateUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by LuoLiBing on 17/1/9.
 */
@Converter
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, Date> {
    @Override
    public Date convertToDatabaseColumn(LocalDate localDate) {
        return (localDate == null ? null : DateUtil.convertToDate(localDate));
    }

    @Override
    public LocalDate convertToEntityAttribute(Date dbData) {
        return (dbData == null ? null : DateUtil.convertLocalDate(dbData));
    }

}

