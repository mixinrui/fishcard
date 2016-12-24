package com.boxfishedu.workorder.entity.mongo;

import com.boxfishedu.workorder.requester.resultbean.NetAnalysisBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.springframework.beans.BeanUtils;

/**
 * Created by hucl on 16/12/24.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity(noClassnameStored = true)
public class NetPingAnalysisInfo {

    private String role;

    private double maxServicePing;

    private double maxInternetPing;

    private double averageServicePing;

    private double averageSInternetPing;

    private double minServicePing;

    private double minInternetPing;

    private void analysis(NetAnalysisBean netAnalysisBean){
        BeanUtils.copyProperties(netAnalysisBean,this);
    }
}
