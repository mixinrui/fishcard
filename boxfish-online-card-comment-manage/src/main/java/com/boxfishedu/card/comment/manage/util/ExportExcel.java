package com.boxfishedu.card.comment.manage.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by ansel on 16/11/30.
 */
public class ExportExcel {

    private final static Logger logger = LoggerFactory.getLogger(ExportExcel.class);

    private HSSFSheet sheet;
    private HSSFWorkbook workbook;

    public ExportExcel(HSSFWorkbook workbook, HSSFSheet sheet){
        this.sheet=sheet;
        this.workbook=workbook;
    }
    /**
     * 默认开始行
     */
    public static final int ROW_START=1;
    /**
     * 默认列宽
     */
    public static final double COLUMN_WIDTH=15.99;
    /**
     * GET
     */
    public static final String GET="get";

    public static final String IS = "is";

    public static final String SEUID="serialVersionUID";


    /**
     * 获取表头
     * @param columnHeader
     */
    public void createColumHeader(String[] columnHeader) {

        // 设置列头
        HSSFRow row2 = sheet.createRow(0);

        // 指定行高
        row2.setHeight((short) 400);


        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 指定单元格居中对齐
        cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 指定单元格垂直居中对齐
        cellStyle.setWrapText(true);// 指定单元格自动换行

        // 单元格字体
        HSSFFont font = workbook.createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontName("宋体");
        font.setFontHeight((short) 200);
        cellStyle.setFont(font);

        // 表格边框
        cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 设置单无格的边框为粗体
        cellStyle.setBottomBorderColor(HSSFColor.BLACK.index); // 设置单元格的边框颜色．
        cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
        cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
        cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyle.setTopBorderColor(HSSFColor.BLACK.index);

        // 设置单元格背景色
        cellStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        HSSFCell cell3;
        for (int i = 0; i < columnHeader.length; i++) {
            cell3 = row2.createCell(i);
            cell3.setCellType(HSSFCell.ENCODING_UTF_16);
            cell3.setCellStyle(cellStyle);
            cell3.setCellValue(new HSSFRichTextString(columnHeader[i]));
            //  sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i,(int)COLUMN_WIDTH*2*256);
        }
    }
    /**
     * 生成列，并写入值方法，根据传进去的LIST 的长度生成行，根据 MAP 的size 生成列
     * @param list
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void createExcelFiles(List<Map<String, Object>> list)
            throws FileNotFoundException, IOException {
        HSSFRow row=null;
        Map<String,Object> mm=null;
        for(int i=0;i<list.size();i++){
            row=sheet.createRow(ROW_START+i);//创建行
            mm=list.get(i);
            int k=0;
            //创建单元格
            HSSFCell cell=null;
            for (Map.Entry<String, Object> m :mm.entrySet()) {
                cell=row.createCell(k++);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                cell.setCellValue(m.getValue()==null?null:m.getValue().toString().trim());
            }
        }
        //设置单元格格式
        //给单元格赋值
        //workbook.write(stream)
    }
    /**
     * 根据传的COLUMNS 只导出部分列的数据
     * @param list
     * @param columns 导出的列名 e.g:["SUBJECT_NAME","SID"]
     * @throws FileNotFoundException
     * @throws IOException
     *
     */
    public void createExcelFiles(List<Map<String, Object>> list, String[] columns, HttpServletResponse response, String fileName)
            throws FileNotFoundException, IOException {

        HSSFRow row=null;
        Map<String,Object> mm=null;

        HSSFCellStyle numCellStyle = getNumCellStyle(workbook);
        HSSFCellStyle wordsCellStyle = getWordsCellStyle(workbook);
        for(int i=0;i<list.size();i++){

            row=sheet.createRow(ROW_START+i);//创建行
            mm=list.get(i);
            int k=0;
            //创建单元格
            HSSFCell cell=null;
            for (String s:columns) {
                cell=row.createCell(k++);

                // 数字居右
                if(mm.get(s)!=null&& mm.get(s).toString().contains(".")) {
                    cell.setCellStyle(numCellStyle);
                }
                // 文字居左
                else {
                    cell.setCellStyle(wordsCellStyle);
                }

                cell.setCellValue(mm.get(s)==null?null:mm.get(s).toString().trim());
            }

        }
        //设置单元格格式
        //给单元格赋值

        download(response, fileName);
    }
    public void download(HttpServletResponse response, String fileName)
            throws IOException {
        response.setContentType("application/vnd.ms-excel");
//		name=new String(name.getBytes("GB2312"),"ISO-8859-1");
        response.addHeader("Content-Disposition", "attachment;filename="+java.net.URLEncoder.encode(fileName+".xls","UTF-8"));
        try(OutputStream os=response.getOutputStream()) {
//			os.flush();
            workbook.write(os);
        }
    }


    /**
     * 导出EXCEL 请先调用 createColumHeader创建表头  之后将LIST 传入
     * @param list 传入的LIST
     * @param fields   为空，导出会部属性，不为空，则根据传入的属性导出
     * @e.g.
     *  例如 start
     *  String[] columnHead={"id","名字","开启时间","关闭时间","类型"};
    //String[] columns={"SID","SUBJECT_NAME","DEBIT","CREDIT"};
    String fileName="会计期间表";

    e.createColumHeader(columnHead);
    e.createExcelFilesWithList(model.getAll(), getResponse(), fileName,new String[]{"id","name","offTime","onTime","type"});
    end .
     * @throws FileNotFoundException
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void createExcelFilesWithList(List<?> list, String[] fields)
            throws IOException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException {

        HSSFCellStyle style=workbook.createCellStyle();
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

        if(org.springframework.util.ObjectUtils.isEmpty(list)) {
            return;
        }

        Map<String, Method> methodMap = resolveGetMethods(list.get(0).getClass(), fields);

        IntStream.range(0, list.size()).forEach(index -> {
            HSSFRow row=sheet.createRow(ROW_START+index);//创建行
            Object obj=list.get(index);
            HSSFCell cell=null;
            for (int i = 0, len = fields.length; i < len; i++) {
                String fieldName = fields[i];

                Method method = methodMap.get(fieldName);
                if(method == null) {
                    continue;
                }

                Object result = null;
                try {
                    result = method.invoke(obj);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                cell=row.createCell(i);
                cell.setCellStyle(style);
                if (result instanceof Number) {
                    cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
                } else {
                    cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                }
                setCellValue(cell, result);
            }
        });
    }

    private void setCellValue(HSSFCell cell, Object obj) {
        if(obj == null) {
            cell.setCellValue("");
        } else {
            if (obj instanceof Number) {
                cell.setCellValue(Double.valueOf(obj.toString()));
            } else {
                cell.setCellValue(obj.toString());
            }
        }
    }


    /**
     * 获取给定字段和类的所有存在的get或者is方法
     * @param clazz
     * @param fieldNames
     * @return
     */
    private Map<String, Method> resolveGetMethods(Class<?> clazz, String[] fieldNames) {
        return Stream.of(fieldNames)
                .map(f -> resolveGetMethod(clazz, f))
                .filter(f -> f != null)
                .collect(Collectors.toMap(
                        this::unResolveFieldName, Function.identity()
                ));
    }


    private Method resolveGetMethod(Class<?> clazz, String fieldName) {

        String fName = fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
        String getMethodName = GET + fName;
        try {
            return clazz.getMethod(getMethodName);
        } catch (NoSuchMethodException e) {
            String isMethodName = IS + fName;
            try {
                return clazz.getMethod(isMethodName);
            } catch (NoSuchMethodException e1) {
                return null;
            }
        }
    }

    /**
     * 根据方法反推属性
     * @param method
     * @return
     */
    private String unResolveFieldName(Method method) {
        String name = method.getName();
        if(name.startsWith(GET)) {
            String f1 = name.substring(3);
            return f1.substring(0, 1).toLowerCase() + f1.substring(1);
        } else {
            String f1 = name.substring(2);
            return f1.substring(0, 1).toLowerCase() + f1.substring(1);
        }
    }


    /**
     * 获取数字行样式
     * @param wb
     * @return
     */
    private HSSFCellStyle getNumCellStyle(HSSFWorkbook wb) {

        HSSFCellStyle style = wb.createCellStyle();
        // 表格边框
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 设置单无格的边框为粗体
        style.setBottomBorderColor(HSSFColor.BLACK.index); // 设置单元格的边框颜色．
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);

        // 对齐方式
        style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 指定单元格垂直居中对齐
        return style;

    }


    /**
     * 获取汉字样式
     * @param wb
     * @return
     */
    private HSSFCellStyle getWordsCellStyle(HSSFWorkbook wb) {

        HSSFCellStyle style = wb.createCellStyle();
        // 表格边框
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 设置单无格的边框为粗体
        style.setBottomBorderColor(HSSFColor.BLACK.index); // 设置单元格的边框颜色．
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);

        // 对齐方式
        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 指定单元格垂直居中对齐

        return style;

    }

}

