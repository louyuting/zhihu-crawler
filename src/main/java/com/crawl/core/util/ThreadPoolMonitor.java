package com.crawl.core.util;

import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;

/**
 * 线程池工具类，监视ThreadPoolExecutor执行情况
 */
public class ThreadPoolMonitor implements Runnable{
    private static Logger logger = Constants.MONITOR_LOGGER;

    private static volatile boolean isStopMonitor = false;
    private ThreadPoolExecutor executor;
    private String threadPoolName = "";

    /**
     * @param executor 线程池
     * @param threadPoolName  线程池 threadPoolName
     */
    public ThreadPoolMonitor(ThreadPoolExecutor executor, String threadPoolName){
        this.executor = executor;
        this.threadPoolName = threadPoolName;
    }

    public void run(){
        while(!isStopMonitor){
            logger.info(threadPoolName + String.format("[monitor] [%d/%d] Active: %d, Completed: %d, queueSize: %d, TotalTask: %d, isShutdown: %s, isTerminated: %s",
                            this.executor.getPoolSize(),     // 获取当前线程池中线程数量
                            this.executor.getCorePoolSize(), // 获取线程池中线程核心数量
                            this.executor.getActiveCount(),  // 获取线程池中正在运行的线程数量
                            this.executor.getCompletedTaskCount(), // 获取当前已经运行完成的task
                            this.executor.getQueue().size(),       // 获取当前阻塞队列中task数量
                            this.executor.getTaskCount(),          // 获取总的task数量，包括结束的和正在运行的和阻塞队列中的。
                            this.executor.isShutdown(),            // 线程池是否已经不再接受任务
                            this.executor.isTerminated()));        // 线程池是否已经终止
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("InterruptedException",e);
            }
        }
    }

    public static void setIsStopMonitor(boolean isStopMonitor) {
        ThreadPoolMonitor.isStopMonitor = isStopMonitor;
    }
}