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
 * 线程池管理(线程统一调度管理)
 */
@SuppressWarnings("ALL")
@Component
public class ThreadPoolManager<T extends Runnable> {
    @Autowired
    private PoolConf poolConf;

    // 任务缓冲队列
    private Queue<T> taskQueue = new ConcurrentLinkedQueue<T>();

    // 线程池超出界线时将任务加入缓冲队列,此处可以加到redis等外存储设备,防止本机内存泄漏问题发生
    private RejectedExecutionHandler rejectedExecutionHandler = null;

    // 将缓冲队列中的任务重新加载到线程池
    private Runnable accessBufferThread = null;

    // 创建一个调度线程池
    private ScheduledExecutorService scheduler = null;

    //通过调度线程周期性的执行缓冲队列中任务
    private ScheduledFuture<?> taskHandler = null;

    // 线程池
    protected ThreadPoolExecutor threadPoolExecutor = null;

    private final Lock lock = new ReentrantLock();

    public void perpare() {
        if (threadPoolExecutor.isShutdown() && !threadPoolExecutor.prestartCoreThread()) {
            int startThread = threadPoolExecutor.prestartAllCoreThreads();
        }
    }

    //向线程池中添加任务方法
    public void addExecuteTask(Runnable task) {
        if (task != null) {
            threadPoolExecutor.execute(task);
        }
    }

    protected boolean isTaskEnd() {
        return (threadPoolExecutor.getActiveCount() == 0);
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

    private synchronized List<T> getElementsFromQueueTask(){
        List<T> runnables=new ArrayList<>();
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
            List<T> runnables=getElementsFromQueueTask();
            runnables.forEach(runnable -> threadPoolExecutor.execute(runnable));
        };
        taskHandler = scheduler.scheduleAtFixedRate(accessBufferThread, 0,
                poolConf.getPeriod_task_qos(), TimeUnit.MILLISECONDS);

        rejectedExecutionHandler = (task, executor) -> {
            taskQueue.offer((T) task);
        };
        threadPoolExecutor = new ThreadPoolExecutor(poolConf.getSize_core_pool(), poolConf.getSize_max_pool(),
                poolConf.getTime_keep_alive(), TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(poolConf.getSize_work_queue()), rejectedExecutionHandler);
    }

    public void execute(Thread thread) {
        threadPoolExecutor.execute(thread);
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



    public String monitor() {
        BlockingQueue<Runnable> poolQueue = threadPoolExecutor.getQueue();
        int queueSize_using = poolQueue.size();
        int queueSize_remaining = poolQueue.remainingCapacity();
        int queueSize_count = queueSize_using + queueSize_remaining;
        String poolInfo = String.format(
                " ThreadPool type: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d, queueSize_count: %d, queueSize_using: %d), " +
                        "Task: %d (completed: %d), Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)",
                "http", threadPoolExecutor.getPoolSize(), threadPoolExecutor.getActiveCount(), threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor.getLargestPoolSize(), queueSize_count, queueSize_using,
                threadPoolExecutor.getTaskCount(), threadPoolExecutor.getCompletedTaskCount(), threadPoolExecutor.isShutdown(),
                threadPoolExecutor.isTerminated(), threadPoolExecutor.isTerminating());
        return poolInfo;
    }

    public void testStart() {
        for( int i=0;i<100000;i++){
            threadPoolExecutor.execute(() -> {System.out.println("线程:"+Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                }
                catch (Exception ex){
                    System.out.println(ex);
                }
            });
        }
    }
}