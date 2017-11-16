package com.crawl.proxy;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.core.util.HttpClientUtil;
import com.crawl.core.util.SimpleThreadPoolExecutor;
import com.crawl.core.util.ThreadPoolMonitor;
import com.crawl.proxy.entity.Proxy;
import com.crawl.proxy.task.ProxyPageTask;
import com.crawl.proxy.task.ProxySerializeTask;
import org.slf4j.Logger;

public class ProxyHttpClient {
    private static final Logger logger = Constants.PROXY_LOGGER;
    private volatile static ProxyHttpClient instance;

    public static ProxyHttpClient getInstance(){
        if (instance == null){
            synchronized (ProxyHttpClient.class){
                if (instance == null){
                    instance = new ProxyHttpClient();
                }
            }
        }
        return instance;
    }
    /**
     * 代理测试线程池
     */
    private ThreadPoolExecutor proxyTestThreadExecutor;
    /**
     * 代理网站下载线程池
     */
    private ThreadPoolExecutor proxyDownloadThreadExecutor;

    public ProxyHttpClient(){
        initThreadPool();
        initProxy();
    }
    /**
     *
     * 初始化线程池
     */
    private void initThreadPool(){
        proxyTestThreadExecutor = new SimpleThreadPoolExecutor(100, 100,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10000),
                new ThreadPoolExecutor.DiscardPolicy(),
                "proxyTestThreadExecutor");
        proxyDownloadThreadExecutor = new SimpleThreadPoolExecutor(5, 5,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10000), "proxyDownloadThreadExecutor");
        new Thread(new ThreadPoolMonitor(proxyTestThreadExecutor, "proxyTestThreadExecutor")).start();
        new Thread(new ThreadPoolMonitor(proxyDownloadThreadExecutor, "proxyDownloadThreadExecutor")).start();
    }

    /**
     * 初始化proxy
     */
    private void initProxy(){
        Proxy[] proxyArray = null;
        try {
            proxyArray = (Proxy[]) HttpClientUtil.deserializeObject(Config.proxyPath);
            int usableProxyCount = 0;
            for (Proxy p : proxyArray){
                if (p == null){
                    continue;
                }
                p.setTimeInterval(Constants.TIME_INTERVAL);
                p.setFailureTimes(0);
                p.setSuccessfulTimes(0);
                long nowTime = System.currentTimeMillis();
                if (nowTime - p.getLastSuccessfulTime() < 1000 * 60 * 60 * 24){
                    //上次成功离现在少于一天
                    ProxyPool.proxyQueue.add(p);
                    ProxyPool.proxySet.add(p);
                    usableProxyCount++;
                }
            }
            logger.info("proxies文件反序列化proxy成功，" + proxyArray.length + "个代理,可用代理" + usableProxyCount + "个");
        } catch (Exception e) {
            logger.warn("proxies文件反序列化proxy失败");
        }
    }
    /**
     * 抓取代理
     */
    public void startCrawl(){
        new Thread(()-> {
                while (true){
                    int count =0;
                    for (String url : ProxyPool.proxyMap.keySet()){
                        count++;
                        /**
                         * 首次本机直接下载代理页面
                         */
                        proxyDownloadThreadExecutor.execute(new ProxyPageTask(url, false));
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        logger.info("proxyMap current parse number is:" + count);
                    }
                    logger.info("proxyMap parse over");
                    // 优雅的关闭下载代理IP的线程池
                    proxyDownloadThreadExecutor.shutdown();
                    break;
                }
            }
        ).start();
        manageProxyClient();
        new Thread(new ProxySerializeTask()).start();
    }
    public ThreadPoolExecutor getProxyTestThreadExecutor() {
        return proxyTestThreadExecutor;
    }

    public ThreadPoolExecutor getProxyDownloadThreadExecutor() {
        return proxyDownloadThreadExecutor;
    }

    private void manageProxyClient(){
        while (true){
            String logInfo = String.format("ProxyPool.proxyQueue.size=%d, ProcyPool.proxyMap.size=%d,ProcyPool.proxySet.size=%d",
                ProxyPool.proxyQueue.size(), ProxyPool.proxyMap.size(), ProxyPool.proxySet.size());
            logger.info("proxy info: {}", logInfo);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}