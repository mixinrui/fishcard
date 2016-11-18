package com.boxfishedu.workorder.service.instantclass;

import com.boxfishedu.workorder.common.exception.BusinessException;
import com.google.common.collect.Maps;
import lombok.Data;

/**
 * Created by hucl on 16/11/8.
 */
@Data
public class InstantRecommandAlthom {
    private Integer countStart;
    private Integer countEnd;
    private final Integer CYCLE=3;
    private static Integer CYCLE_COUNT=0;

    public static java.util.Map<Integer,Integer> indexMap= Maps.newHashMap();

    static{
        indexMap.put(1,2);
        indexMap.put(2,8);
        indexMap.put(0,32);

        indexMap.values().forEach(value->CYCLE_COUNT+=value);
    }


    public InstantRecommandAlthom(Integer accessIndex){
        //倍数
        Integer multiple  =accessIndex/CYCLE;
        //余数
        Integer remainder =accessIndex%CYCLE;
        switch (remainder){
            case 1:
                this.countStart=CYCLE_COUNT*multiple;
                break;
            case 2:
                this.countStart=CYCLE_COUNT*multiple+indexMap.get(1);
                break;
            case 0:
                this.countStart=CYCLE_COUNT*(multiple-1)+indexMap.get(1)+indexMap.get(2);
                break;
            default:
                throw new BusinessException("不合理的倍数");
        }
        this.countEnd=this.countStart+indexMap.get(remainder);
    }

    public InstantRecommandAlthom(){

    }

    public static void main(String[] args) {
        System.out.println(new InstantRecommandAlthom(8));
    }
}