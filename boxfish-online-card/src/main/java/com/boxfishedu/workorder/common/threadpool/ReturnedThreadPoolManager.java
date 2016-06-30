package com.boxfishedu.workorder.common.threadpool;

import com.boxfishedu.workorder.common.config.PoolConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hucl on 16/4/9.
 */
@Component
public class ReturnedThreadPoolManager<T> {
    @Autowired
    private PoolConf poolConf;

    // 任务缓冲队列
    private Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<Runnable>();

    // 线程池超出界线时将任务加入缓冲队列,此处可以加到redis等外存储设备,防止本机内存泄漏问题发生
    private RejectedExecutionHandler rejectedExecutionHandler = null;

    // 将缓冲队列中的任务重新加载到线程池
    private Runnable accessBufferThread = null;

    // 创建一个调度线程池
    private ScheduledExecutorService scheduler = null;

    //通过调度线程周期性的执行缓冲队列中任务
    private ScheduledFuture<?> taskHandler = null;

    private ExecutorService threadPoolExecutor = null;

    private final Lock lock = new ReentrantLock();

    //向线程池中添加任务方法
    public void addExecuteTask(Runnable task) {
        if (task != null) {
            threadPoolExecutor.execute(task);
        }
    }

    protected boolean isTaskEnd() {
        return threadPoolExecutor.isTerminated();
    }

    public void shutdown() {
        taskQueue.clear();
        threadPoolExecutor.shutdown();
    }

    // 消息队列检查方法
    private boolean hasMoreAcquire() {
        return !taskQueue.isEmpty();
    }

    private int getQueueSizeAndMaxPool(){
        return poolConf.getSize_max_pool()+poolConf.getSize_work_queue();
    }

    private synchronized List<Runnable> getElementsFromQueueTask(){
        List<Runnable> runnables=new ArrayList<>();
        if(taskQueue.size()>getQueueSizeAndMaxPool()){
            for (int i=0;i<getQueueSizeAndMaxPool();i++){
                runnables.add(taskQueue.poll());
            }
        }
        else{
            runnables.addAll(taskQueue);
            taskQueue.clear();
        }
        return runnables;
    }

    @PostConstruct
    public void init() {
        scheduler = Executors.newScheduledThreadPool(1);
        accessBufferThread = () -> {
            if(!hasMoreAcquire()){
                return;
            }
            List<Runnable> runnables=getElementsFromQueueTask();
            runnables.forEach(runnable -> threadPoolExecutor.submit(runnable));
        };
        taskHandler = scheduler.scheduleAtFixedRate(accessBufferThread, 0,
                poolConf.getPeriod_task_qos(), TimeUnit.MILLISECONDS);

        rejectedExecutionHandler = (task, executor) -> {
            System.out.println("开始拒绝服务,加入taskqueue:队列的大小为:"+taskQueue.size());
            taskQueue.offer(task);
        };

        threadPoolExecutor = new ThreadPoolExecutor(poolConf.getSize_core_pool(), poolConf.getSize_max_pool(),
                poolConf.getTime_keep_alive(), TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(poolConf.getSize_work_queue()), rejectedExecutionHandler);
//        threadPoolExecutor =  Executors.newCachedThreadPool();
    }

    public void execute(Thread thread) {
        threadPoolExecutor.execute(thread);
    }

    public  <R> Future<R> submit(Callable<R> callable) {
        return threadPoolExecutor.submit(callable);
    }

    public void execute(Thread... threads) {
        for (Thread thread : threads) {
            execute(thread);
        }
    }

    public void join(Thread thread) throws Exception {
        thread.join();
    }

    public void join(Thread... threads) throws Exception {
        for (Thread thread : threads) {
            execute(thread);
        }
    }

    public void executeAndJoin(Thread... threads) throws Exception {
        execute(threads);
        join(threads);
    }

    public void handleException(Future... futures) throws Exception{
        for(Future future:futures){
            future.get();
        }
    }
}
