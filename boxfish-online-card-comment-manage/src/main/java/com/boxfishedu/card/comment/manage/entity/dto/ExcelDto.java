package com.boxfishedu.card.comment.manage.entity.dto;

import com.boxfishedu.card.comment.manage.exception.ExcelException;
import com.boxfishedu.card.comment.manage.util.ExportExcel;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by ansel on 16/11/30.
 */
public abstract class ExcelDto {

    protected final ExportExcel excel;

    protected final List<?> data;

    public ExcelDto(List<?> data, String sheetName) {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetName);
        excel = new ExportExcel(workbook, sheet);
        this.data = data;
    }

    public void downLoad(HttpServletResponse response, String fileName) {
        try {
            excel.createColumHeader(getHeaderName());
            excel.createExcelFilesWithList(data, getFields());
            excel.download(response, fileName);
        } catch (IOException |IllegalAccessException | InvocationTargetException e) {
            throw new ExcelException(e);
        }
    }

    abstract String[] getHeaderName();

    abstract String[] getFields();
}