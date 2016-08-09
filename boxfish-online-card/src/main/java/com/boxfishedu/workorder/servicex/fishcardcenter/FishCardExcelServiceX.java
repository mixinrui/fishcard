package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.servicex.bean.WorkOrderView;
import com.boxfishedu.workorder.servicex.bean.WorkOrderViewExcel;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import com.boxfishedu.workorder.common.util.DateUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 后台导出Excel
 * Created by jiaozijun on 16/8/5.
 */
@Component
public class FishCardExcelServiceX {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FishCardQueryServiceX fishCardQueryServiceX;

    /**
     * 导出Excel
     * @param fishCardFilterParam
     * @param response
     * @param pageable
     */
    public void exportExcel(FishCardFilterParam fishCardFilterParam, HttpServletResponse response, Pageable pageable) {
        List<WorkOrder> listWorkOrder = fishCardQueryServiceX.listFishCardsByUnlimitedUserCondForExcel(fishCardFilterParam,pageable);
        if(null == listWorkOrder || listWorkOrder.isEmpty()){
            return;
        }

        List<WorkOrderViewExcel> list =  changeWorkOrder(listWorkOrder);

        final String fileName = "fishCard_" + DateUtil.Date2String(new Date()) + "_" + pageable.getPageNumber();
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow hssfRow = hssfSheet.createRow(0);

        HSSFCell hssfCell = hssfRow.createCell(0);
        hssfCell.setCellValue("鱼卡号");

        hssfCell = hssfRow.createCell(1);
        hssfCell.setCellValue("鱼卡创建时间");

        hssfCell = hssfRow.createCell(2);
        hssfCell.setCellValue("学生ID");

        hssfCell = hssfRow.createCell(3);
        hssfCell.setCellValue("学生姓名");

        hssfCell = hssfRow.createCell(4);
        hssfCell.setCellValue("教师ID");

        hssfCell = hssfRow.createCell(5);
        hssfCell.setCellValue("教师姓名");

        hssfCell = hssfRow.createCell(6);
        hssfCell.setCellValue("课程类型");

        hssfCell = hssfRow.createCell(7);
        hssfCell.setCellValue("课程名");

        hssfCell = hssfRow.createCell(8);
        hssfCell.setCellValue("鱼卡状态");

        hssfCell = hssfRow.createCell(9);
        hssfCell.setCellValue("计划上课开始时间");

        hssfCell = hssfRow.createCell(10);
        hssfCell.setCellValue("计划上课结束时间");

        hssfCell = hssfRow.createCell(11);
        hssfCell.setCellValue("实际上课开始时间");

        hssfCell = hssfRow.createCell(12);
        hssfCell.setCellValue("实际上课结束时间");

        hssfCell = hssfRow.createCell(13);
        hssfCell.setCellValue("所属订单");




        for(int i = 0;i < list.size();i++){
            hssfRow = hssfSheet.createRow(i + 1);

            hssfCell = hssfRow.createCell(0);
            hssfCell.setCellValue( list.get(i).getId() );

            hssfCell = hssfRow.createCell(1);
            hssfCell.setCellValue( list.get(i).getCreateTime() );


            hssfCell = hssfRow.createCell(2);
            hssfCell.setCellValue( list.get(i).getStudentId() );


            hssfCell = hssfRow.createCell(3);
            hssfCell.setCellValue( list.get(i).getStudentName() );

            hssfCell = hssfRow.createCell(4);
            hssfCell.setCellValue( list.get(i).getTeacherId() );

            hssfCell = hssfRow.createCell(5);
            hssfCell.setCellValue( list.get(i).getTeacherName() );

            hssfCell = hssfRow.createCell(6);
            hssfCell.setCellValue( list.get(i).getCourseType() );

            hssfCell = hssfRow.createCell(7);
            hssfCell.setCellValue( list.get(i).getCouserName() );

            hssfCell = hssfRow.createCell(8);
            hssfCell.setCellValue( list.get(i).getStatus() );

            hssfCell = hssfRow.createCell(9);
            hssfCell.setCellValue( list.get(i).getPlanStartTime() );

            hssfCell = hssfRow.createCell(10);
            hssfCell.setCellValue( list.get(i).getPlanEndTime());

            hssfCell = hssfRow.createCell(11);
            hssfCell.setCellValue( list.get(i).getRealStartTime());

            hssfCell = hssfRow.createCell(12);
            hssfCell.setCellValue( list.get(i).getRealEndTime());


            hssfCell = hssfRow.createCell(13);
            hssfCell.setCellValue( list.get(i).getOrderCode());

        }

        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
        response.setContentType("application/msexcel");
        try(OutputStream out = response.getOutputStream()) {
            hssfWorkbook.write(out);
            logger.info("导出鱼卡列表成功!");
        }catch (Exception e){
            e.printStackTrace();
            logger.error("导出鱼卡列表异常!");
        }



    }

    private List<WorkOrderViewExcel> changeWorkOrder(List<WorkOrder> list){
        List<WorkOrderViewExcel> listexcel = Lists.newArrayList();

        for (WorkOrder workOrder :list){
            WorkOrderViewExcel wexcel = new  WorkOrderViewExcel();
        }


        list.forEach(workOrder -> {
            WorkOrderViewExcel wexcel = new  WorkOrderViewExcel();
               wexcel.setId(workOrder.getId());
               wexcel.setCreateTime(workOrder.getCreateTime());
               wexcel.setStudentId(workOrder.getStudentId());
               wexcel.setStudentName(workOrder.getStudentName());
               wexcel.setTeacherId(workOrder.getTeacherId());
               wexcel.setTeacherName(workOrder.getTeacherName());
               wexcel.setCourseType(workOrder.getCourseType());
               wexcel.setCouserName(workOrder.getCourseName());
               wexcel.setStatus(workOrder.getStatus());
               wexcel.setPlanStartTime(workOrder.getStartTime());
               wexcel.setPlanEndTime(workOrder.getEndTime());

               wexcel.setRealStartTime(workOrder.getActualStartTime());
               wexcel.setRealEndTime(workOrder.getActualEndTime());
               wexcel.setOrderCode(workOrder.getOrderCode());
            listexcel.add(wexcel);
        });

        return listexcel;
    }
}
