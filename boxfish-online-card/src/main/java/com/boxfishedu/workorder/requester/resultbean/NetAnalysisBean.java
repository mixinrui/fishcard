package com.boxfishedu.workorder.requester.resultbean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by hucl on 16/12/22.
 */
@Data
public class NetAnalysisBean {

    @JsonIgnore
    private final static String MAX = "MAX";
    @JsonIgnore
    private final static String MIN = "MIN";
    @JsonIgnore
    private final static String AVERAGE = "AVERAGE";
    @JsonIgnore
    private final static String SERVICE_PING = "service_ping";
    @JsonIgnore
    private final static String INTERNET_PING = "internet_ping";

    @JsonIgnore
    //"student,teacher"
    private String role;

    @JsonIgnore
    private double maxServicePing;
    @JsonIgnore
    private double maxInternetPing;
    @JsonIgnore
    private double averageServicePing;
    @JsonIgnore
    private double averageInternetPing;
    @JsonIgnore
    private double minServicePing;
    @JsonIgnore
    private double minInternetPing;

    private List<java.util.Map<String, Object>> details = Lists.newArrayList();

    public NetAnalysisBean netAdapter(NetSourceBean netSourceBean) {
        if (Objects.isNull(netSourceBean) || CollectionUtils.isEmpty(netSourceBean.getContent())) {
            return this;
        }

        netSourceBean.getContent().forEach(contentBean -> {
            Map map = Maps.newHashMap();
            map.putAll(contentBean.getProperties());
            map.put("appTime", new Date(contentBean.getAppTime()));
            map.put("sysTime", new Date(contentBean.getSysTime()));
            this.getDetails().add(map);
        });

        this.wrapAnalysis();

        return this;
    }

    private void wrapAnalysis() {
        this.setMaxServicePing(this.max(SERVICE_PING));
        this.setMaxInternetPing(this.max(INTERNET_PING));

        this.setMinServicePing(this.min(SERVICE_PING));
        this.setMinInternetPing(this.min(INTERNET_PING));

        this.setAverageServicePing(this.average(SERVICE_PING));
        this.setAverageInternetPing(this.average(INTERNET_PING));
    }

    private double getPing(Map<String, Object> detailMap, String pingType) {
        return StringUtils.isEmpty(String.valueOf(detailMap.get(pingType)))
                ? 0 : Double.parseDouble(detailMap.get(pingType).toString());
    }

    private double max(String pingType) {
        Object maxItem = this.getDetails().stream()
                             .filter(map -> getPing(map, pingType) > 0)
                             .max(Comparator.comparing(map -> getPing(map, pingType)))
                             .get()
                             .get(pingType);

        if (Objects.isNull(maxItem)) {
            return 0;
        }

        return Double.parseDouble(maxItem.toString());
    }

    private double min(String pingType) {
        Object maxItem = this.getDetails().stream()
                             .filter(map -> getPing(map, pingType) > 0)
                             .min(Comparator.comparing(map -> getPing(map, pingType)))
                             .get()
                             .get(pingType);

        if (Objects.isNull(maxItem)) {
            return 0;
        }

        return Double.parseDouble(maxItem.toString());
    }

    private double average(String pingType) {
        return this.getDetails().stream()
                   .filter(map -> getPing(map, pingType) > 0)
                   .mapToDouble(map -> getPing(map, pingType))
                   .average()
                   .getAsDouble();
    }

}
