package com.crawl.zhihu;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.core.util.SimpleThreadPoolExecutor;
import com.crawl.core.util.ThreadPoolMonitor;
import com.crawl.proxy.ProxyHttpClient;
import com.crawl.zhihu.task.DetailListPageTask;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;

public class ZhiHuUserHttpClient extends AbstractHttpClient{
    private static Logger logger =  Constants.ZHIHU_LOGGER;
    private static Logger sudu_logger =  Constants.SUDU_LOGGER;
    private volatile static ZhiHuUserHttpClient instance;
    /**
     * 统计用户数量
     */
    public static AtomicInteger parseUserCount = new AtomicInteger(0);
    private static long startTime = System.currentTimeMillis();
    public static volatile boolean isStop = false;

    public static ZhiHuUserHttpClient getInstance(){
        if (instance == null){
            synchronized (ZhiHuUserHttpClient.class){
                if (instance == null){
                    instance = new ZhiHuUserHttpClient();
                }
            }
        }
        return instance;
    }
    /**
     * 详情列表页下载线程池
     */
    private ThreadPoolExecutor detailListPageThreadPool;

    private ZhiHuUserHttpClient() {
        super();
        initUserThreadPool();
    }

    @Override
    public void startCrawl(String userToken) {
        if(detailListPageThreadPool == null){
            initUserThreadPool();
        }
        new Thread(new ThreadPoolMonitor(detailListPageThreadPool, "detailListPageThreadPool")).start();
        String startUrl = String.format(Constants.USER_FOLLOWEES_URL, userToken, 0);
        HttpGet request = new HttpGet(startUrl);
        request.setHeader("authorization", "oauth " + getAuthorization());
        detailListPageThreadPool.execute(new DetailListPageTask(request, Config.isProxy));
        manageHttpClient();
    }


    /**
     * 初始化线程池
     */
    private void initUserThreadPool(){
        detailListPageThreadPool = new SimpleThreadPoolExecutor(Config.downloadUserPageThreadSize,
                Config.downloadUserPageThreadSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(3000),
                new ThreadPoolExecutor.DiscardPolicy(),
                "detailListPageThreadPool");
        new Thread(new ThreadPoolMonitor(detailListPageThreadPool, "detailListPageThreadPool")).start();
    }

    /**
     * 管理知乎客户端
     * 关闭整个爬虫
     */
    private void manageHttpClient(){
        while (true) {
            manageUserDetailThreadPool();
            if(detailListPageThreadPool.isTerminated() && isStop){
                break;
            }
        }
    }

    private void manageUserDetailThreadPool(){
        /**
         * 下载网页数
         */
        long downloadPageCount = detailListPageThreadPool.getTaskCount();
        if (downloadPageCount >= Config.downloadUserPageCount &&
                !detailListPageThreadPool.isShutdown()) {
            isStop = true;
            ThreadPoolMonitor.setIsStopMonitor(true);
            detailListPageThreadPool.shutdown();
        }
        if(detailListPageThreadPool.isTerminated()){
            //关闭数据库连接
            Map<Thread, Connection> map = DetailListPageTask.getConnectionMap();
            for(Connection cn : map.values()){
                closeConnection(cn);
            }
            //关闭代理检测线程池
            ProxyHttpClient.getInstance().getProxyTestThreadExecutor().shutdownNow();
            //关闭代理下载页线程池
            ProxyHttpClient.getInstance().getProxyDownloadThreadExecutor().shutdownNow();
        }
        double costTime = (System.currentTimeMillis() - startTime) / 1000.0;//单位s
        sudu_logger.debug("抓取速率：" + parseUserCount.get() / costTime + "个/s");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ThreadPoolExecutor getThreadPool() {
        return detailListPageThreadPool;
    }
}
