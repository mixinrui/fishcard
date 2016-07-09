package com.boxfishedu.beans.form;

import lombok.Data;

import java.io.Serializable;

@Data
public class WorkOrderModelReq implements Serializable{

    private Long teacherId;

    private Long studentId;
}
