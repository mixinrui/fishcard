package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.BackOrderService;
import com.boxfishedu.workorder.service.graborder.MakeWorkOrderService;
import com.boxfishedu.workorder.servicex.bean.WorkOrderViewExcel;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.view.base.StudentInfo;
import com.google.common.collect.Lists;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.Comparator;
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


    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private BackOrderService backOrderService;

    @Autowired
    private MakeWorkOrderService makeWorkOrderService;
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

        hssfCell = hssfRow.createCell(14);
        hssfCell.setCellValue("订单类型");





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

            hssfCell = hssfRow.createCell(14);
            hssfCell.setCellValue( list.get(i).getOrderType());

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
               wexcel.setOrderType(workOrder.getOrderTypeDesc());
//            if(workOrder.getOrderChannel().equals(OrderChannelDesc.STANDARD.getCode() )){
//                wexcel.setOrderType(workOrder.getComboType());
//            }else {
//                wexcel.setOrderType(workOrder.getOrderChannel());
//            }
            listexcel.add(wexcel);
        });

        return listexcel;
    }





    private List<WorkOrder> getSortOrders(List<WorkOrder> workOrders){
        workOrders.sort(new Comparator<WorkOrder>() {
            @Override
            public int compare(WorkOrder o1, WorkOrder o2) {
                if(o1.getStartTime().after(o2.getStartTime())){
                    return -1;
                }
                return 0;
            }
        });

        return workOrders;
    }






    public void exportExcelbuke(HttpServletResponse response) {
        List<WorkOrder> listWorkOrder = makeWorkOrderService.findWorkOrdersTodyTomoAndNeed();
        if (null == listWorkOrder || listWorkOrder.isEmpty()) {
            return;
        }

        List<WorkOrder> listWorkOrderlast = Lists.newArrayList();
        for(WorkOrder workOrder :listWorkOrder){
            List<WorkOrder>  myorders = getSortOrders(  backOrderService .findByOrderId(workOrder.getOrderId()));
            if(myorders.get(0).getId() == workOrder.getId()){
                listWorkOrderlast.add(workOrder);
            }

        }

        List<StudentInfo> studentInfos = Lists.newArrayList();
        listWorkOrderlast.stream().forEach(workOrder -> {
            StudentInfo stu =teacherStudentRequester.getStudentInfo(workOrder.getStudentId());
            stu.setStartTime(DateUtil.Date2String(workOrder.getStartTime())  );
            stu.setFishcardId(workOrder.getId());
            stu.setStudentId(workOrder.getStudentId());
            studentInfos.add(stu);
        });



        final String fileName = "fishCard_" + DateUtil.Date2String(new Date()) ;
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow hssfRow = hssfSheet.createRow(0);

        HSSFCell hssfCell = hssfRow.createCell(0);
        hssfCell.setCellValue("鱼卡号");

        hssfCell = hssfRow.createCell(1);
        hssfCell.setCellValue("学生id");

        hssfCell = hssfRow.createCell(2);
        hssfCell.setCellValue("开始上课时间");

        hssfCell = hssfRow.createCell(3);
        hssfCell.setCellValue("学生电话");

        hssfCell = hssfRow.createCell(4);
        hssfCell.setCellValue("学生姓名");

        hssfCell = hssfRow.createCell(5);
        hssfCell.setCellValue("学校");








        for(int i = 0;i < studentInfos.size();i++){
            hssfRow = hssfSheet.createRow(i + 1);

            hssfCell = hssfRow.createCell(0);
            hssfCell.setCellValue( studentInfos.get(i).getFishcardId() );

            hssfCell = hssfRow.createCell(1);
            hssfCell.setCellValue( studentInfos.get(i).getStudentId() );

            hssfCell = hssfRow.createCell(2);
            hssfCell.setCellValue( studentInfos.get(i).getStartTime() );


            hssfCell = hssfRow.createCell(3);
            hssfCell.setCellValue( studentInfos.get(i).getMobile() );

            hssfCell = hssfRow.createCell(4);
            hssfCell.setCellValue( studentInfos.get(i).getUsername() );


            hssfCell = hssfRow.createCell(5);
            hssfCell.setCellValue( studentInfos.get(i).getSchoolName());

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
}
