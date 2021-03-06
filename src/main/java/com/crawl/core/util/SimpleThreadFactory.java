package com.crawl.core.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>ThreadFactory封装</p>
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/7
 */
public class SimpleThreadFactory implements ThreadFactory{
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final String ThreadPoolName;

    public SimpleThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        ThreadPoolName = "threadPool-";
        namePrefix = ThreadPoolName + poolNumber.getAndIncrement() + "-thread-";
    }

    public SimpleThreadFactory(String threadPoolName) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        ThreadPoolName = threadPoolName;
        namePrefix = ThreadPoolName + "-" + poolNumber.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
