package com.boxfishedu.workorder.common.threadpool;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * Created by hucl on 16/4/8.
 */
@Data
@Component
public class ParameterThread {
    @Autowired
    private ReturnedThreadPoolManager returnedThreadPoolManager;
    private Object targetObj;
    private Class<Object> template;

//    public ParameterThread(Object targetObj,Class<Object> template){
//        this.targetObj=targetObj;
//        this.template=template;
//    }

    public void test(int num) throws Exception{
        //创建两个有返回值的任务
        Callable c1 = new MyCallable("A");
        Callable c2 = new MyCallable("B");
        Future[] fetures=new Future[num];
        //执行任务并获取Future对象
        for(int i=0;i<num;i++) {
            if(0==i)
                fetures[i] = returnedThreadPoolManager.submit(new MyCallable("A"));
            else
                fetures[i] = returnedThreadPoolManager.submit(new MyCallable("X" + i));
        }
        //从Future对象上获取任务的返回值，并输出到控制台
        for(int i=0;i<num;i++) {
            System.out.println(">>>" + fetures[i].get().toString());
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        //创建一个线程池
//        ExecutorService pool = Executors.newFixedThreadPool(2);
//        //创建两个有返回值的任务
//        Callable c1 = new MyCallable("A");
//        Callable c2 = new MyCallable("B");
//        //执行任务并获取Future对象
//        Future f1 = pool.submit(c1);
//        Future f2 = pool.submit(c2);
//        //从Future对象上获取任务的返回值，并输出到控制台
//        System.out.println(">>>"+f1.get().toString());
//        System.out.println(">>>"+f2.get().toString());
//        //关闭线程池
//        pool.shutdown();
    }
}

class MyCallable implements Callable {
    private String oid;

    MyCallable(String oid) {
        this.oid = oid;
    }

    @Override
    public Object call() throws Exception {
        if("A".equals(oid)){
            Thread.sleep(5000);
        }
        else{
            Thread.sleep(6000);
        }
        return oid+"任务返回的内容";
    }
}
