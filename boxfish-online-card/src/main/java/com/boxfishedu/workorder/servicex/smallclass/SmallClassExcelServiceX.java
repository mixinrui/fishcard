package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.BackOrderService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.graborder.MakeWorkOrderService;
import com.boxfishedu.workorder.service.instantclass.SmallClassQueryService;
import com.boxfishedu.workorder.servicex.SmallClassViewExcel;
import com.boxfishedu.workorder.servicex.bean.WorkOrderViewExcel;
import com.boxfishedu.workorder.servicex.fishcardcenter.FishCardQueryServiceX;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
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
 * 小班课导出功能
 * Created by jiaozijun on 16/8/5.
 */
@Component
public class SmallClassExcelServiceX {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FishCardQueryServiceX fishCardQueryServiceX;


    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private BackOrderService backOrderService;

    @Autowired
    private MakeWorkOrderService makeWorkOrderService;

    @Autowired
    private WorkOrderService workOrderService;


    @Autowired
    protected SmallClassQueryService smallClassQueryService;
    /**
     * 导出Excel
     * @param publicFilterParam
     * @param response
     * @param pageable
     */
    public void exportExcel(PublicFilterParam publicFilterParam, HttpServletResponse response, Pageable pageable) {
        processDateParam(publicFilterParam);
        List<SmallClass> workOrderList = smallClassQueryService.filterFishCards(publicFilterParam, pageable);

        if(null == workOrderList || workOrderList.isEmpty()){
            return;
        }

        List<SmallClassViewExcel> list =  changeWorkOrder(  getSortOrders(workOrderList)); // 按照时间排序

        final String fileName = "smallclass_" + DateUtil.Date2String(new Date()) + "_" + pageable.getPageNumber();
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = hssfWorkbook.createSheet();
        HSSFRow hssfRow = hssfSheet.createRow(0);

        HSSFCell hssfCell = hssfRow.createCell(0);
        hssfCell.setCellValue("小班课ID");

        hssfCell = hssfRow.createCell(1);
        hssfCell.setCellValue("鱼卡创建时间");



        hssfCell = hssfRow.createCell(2);
        hssfCell.setCellValue("教师ID");

        hssfCell = hssfRow.createCell(3);
        hssfCell.setCellValue("教师姓名");


        hssfCell = hssfRow.createCell(4);
        hssfCell.setCellValue("群组ID");

        hssfCell = hssfRow.createCell(5);
        hssfCell.setCellValue("房间号");



        hssfCell = hssfRow.createCell(6);
        hssfCell.setCellValue("课程类型");

        hssfCell = hssfRow.createCell(7);
        hssfCell.setCellValue("课程名");

        hssfCell = hssfRow.createCell(8);
        hssfCell.setCellValue("状态");

        hssfCell = hssfRow.createCell(9);
        hssfCell.setCellValue("计划上课开始时间");

        hssfCell = hssfRow.createCell(10);
        hssfCell.setCellValue("计划上课结束时间");

        hssfCell = hssfRow.createCell(11);
        hssfCell.setCellValue("实际上课开始时间");

        hssfCell = hssfRow.createCell(12);
        hssfCell.setCellValue("实际上课结束时间");

        hssfCell = hssfRow.createCell(13);
        hssfCell.setCellValue("学生人数");







        for(int i = 0;i < list.size();i++){
            hssfRow = hssfSheet.createRow(i + 1);

            hssfCell = hssfRow.createCell(0);
            hssfCell.setCellValue( list.get(i).getId() );

            hssfCell = hssfRow.createCell(1);
            hssfCell.setCellValue( list.get(i).getCreateTime() );


            hssfCell = hssfRow.createCell(2);
            hssfCell.setCellValue( list.get(i).getTeacherId() );

            hssfCell = hssfRow.createCell(3);
            hssfCell.setCellValue( list.get(i).getTeacherName() );


            hssfCell = hssfRow.createCell(4);
            hssfCell.setCellValue( list.get(i).getGroupId());


            hssfCell = hssfRow.createCell(5);
            hssfCell.setCellValue( list.get(i).getChatRoomId()==null?"":""+list.get(i).getChatRoomId() );



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
            hssfCell.setCellValue( list.get(i).getClassNum()==null?"":""+list.get(i).getClassNum());

        }

        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
        response.setContentType("application/msexcel");
        try(OutputStream out = response.getOutputStream()) {
            hssfWorkbook.write(out);
            logger.info("导出小班课列表成功!");
        }catch (Exception e){
            e.printStackTrace();
            logger.error("导出小班课列表异常!");
        }



    }

    private List<SmallClassViewExcel> changeWorkOrder(List<SmallClass> list){
        List<SmallClassViewExcel> listexcel = Lists.newArrayList();

        list.forEach(smallClass -> {
            SmallClassViewExcel sexcel = new  SmallClassViewExcel();
            sexcel.setId(smallClass.getId());
            sexcel.setCreateTime(smallClass.getCreateTime());
            sexcel.setTeacherId(smallClass.getTeacherId());
            sexcel.setTeacherName(smallClass.getTeacherName());

            sexcel.setGroupId(smallClass.getGroupId());
            sexcel.setChatRoomId(sexcel.getChatRoomId());

            sexcel.setCourseType(smallClass.getCourseType());
            sexcel.setCouserName(smallClass.getCourseName());
            sexcel.setDifficultyLevel(smallClass.getDifficultyLevel());


            sexcel.setStatus(smallClass.getStatus());



            sexcel.setPlanStartTime(smallClass.getStartTime());
            sexcel.setPlanEndTime(smallClass.getEndTime());

            sexcel.setRealStartTime(smallClass.getActualStartTime());
            sexcel.setRealEndTime(smallClass.getActualEndTime());

            sexcel.setClassNum(smallClass.getClassNum());

            listexcel.add(sexcel);
        });

        return listexcel;
    }





    private List<SmallClass> getSortOrders(List<SmallClass> workOrders) {
        workOrders.sort(new Comparator<SmallClass>() {
            @Override
            public int compare(SmallClass o1, SmallClass o2) {
                if (o1.getStartTime().after(o2.getStartTime())) {
                    return -1;
                }
                return 0;
            }
        });

        return workOrders;

    }


    public void processDateParam(PublicFilterParam fishCardFilterParam) {
        if (null == fishCardFilterParam.getBeginDate()) {
            fishCardFilterParam.setBeginDateFormat(DateUtil.String2Date(ConstantUtil.EARLIEST_TIME));
        } else {
            fishCardFilterParam.setBeginDateFormat(DateUtil.String2Date(fishCardFilterParam.getBeginDate()));
        }
        if (null == fishCardFilterParam.getEndDate()) {
            fishCardFilterParam.setEndDateFormat(DateUtil.String2Date(ConstantUtil.LATEST_TIME));
        } else {
            fishCardFilterParam.setEndDateFormat(DateUtil.String2Date(fishCardFilterParam.getEndDate()));
        }

        if (null != fishCardFilterParam.getCreateBeginDate()) {
            fishCardFilterParam.setCreateBeginDateFormat(DateUtil.String2Date(fishCardFilterParam.getCreateBeginDate()));
        }

        if (null != fishCardFilterParam.getCreateEndDate()) {
            fishCardFilterParam.setCreateEndDateFormat(DateUtil.String2Date(fishCardFilterParam.getCreateEndDate()));
        }
    }
}
