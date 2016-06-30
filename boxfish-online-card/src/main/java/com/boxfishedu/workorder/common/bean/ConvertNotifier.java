package com.boxfishedu.workorder.common.bean;


/**
 * Created by hucl on 16/5/19.
 */
public class ConvertNotifier {
    private ConvertNotifier(){

    }
    private static ConvertNotifier convertNotifier=null;
    public static synchronized ConvertNotifier getInstance(){
        if(null==convertNotifier){
            convertNotifier=new ConvertNotifier();
        }
        return convertNotifier;
    }
}
