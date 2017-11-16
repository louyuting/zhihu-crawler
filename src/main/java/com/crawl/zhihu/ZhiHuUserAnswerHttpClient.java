package com.crawl.zhihu;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.core.util.SimpleThreadPoolExecutor;
import com.crawl.core.util.ThreadPoolMonitor;
import com.crawl.zhihu.task.AbstractPageTask;
import com.crawl.zhihu.task.UserAnswerTask;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

public class ZhiHuUserAnswerHttpClient extends AbstractHttpClient {
    private static Logger logger =  Constants.ZHIHU_LOGGER;
    private static Logger sudu_logger =  Constants.SUDU_LOGGER;
    private volatile static ZhiHuUserAnswerHttpClient instance;
    /**
     * 统计用户answer数量
     */
    public static AtomicInteger parseUserAnswerCount = new AtomicInteger(0);
    private static long startTime = System.currentTimeMillis();
    public static volatile boolean isStop = false;
    private static int currentOffset = 1;
    private static final int LIMIT = 10;

    public static ZhiHuUserAnswerHttpClient getInstance(){
        if (instance == null){
            synchronized (ZhiHuUserAnswerHttpClient.class){
                if (instance == null){
                    instance = new ZhiHuUserAnswerHttpClient();
                }
            }
        }
        return instance;
    }

    /**
     * 答案页下载线程池
     */
    private static ThreadPoolExecutor answerPageThreadPool;

    private ZhiHuUserAnswerHttpClient() {
        super();
        initAnswerThreadPool();
    }

    @Override
    public void startCrawl(String userToken){
        if(answerPageThreadPool == null){
            initAnswerThreadPool();
        }

        new Thread(new ThreadPoolMonitor(answerPageThreadPool, "AnswerPageThreadPool")).start();

        String startUrl = String.format(Constants.USER_ANSWER_URL, userToken, 0);
        HttpRequestBase request = new HttpGet(startUrl);
        request.setHeader("authorization", "oauth " + getAuthorization());
        answerPageThreadPool.execute(new UserAnswerTask(request, true, userToken));
        manageHttpClient();
    }

    public static void initAnswerThreadPool(){
        answerPageThreadPool = new SimpleThreadPoolExecutor(Config.downloadUserAnswerThreadSize,
                Config.downloadUserAnswerThreadSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(3000),
                new ThreadPoolExecutor.DiscardPolicy(),
                "AnswerPageThreadPool");
    }

    /**
     * 管理知乎客户端
     * 关闭整个爬虫
     */
    private void manageHttpClient(){
        while (true) {
            manageUserAnswerThreadPool();
            if(answerPageThreadPool.isTerminated() && isStop){
                break;
            }
        }
    }

    private void manageUserAnswerThreadPool(){
        /**
         * 下载网页数
         */
        long downloadPageCount = answerPageThreadPool.getTaskCount();
        if (downloadPageCount >= Config.downloadUserAnswerPageCount && !answerPageThreadPool.isShutdown()) {
            isStop = true;
            ThreadPoolMonitor.setIsStopMonitor(true);
            answerPageThreadPool.shutdown();
        }
        if(answerPageThreadPool.isTerminated()){
            //关闭数据库连接
            Map<Thread, Connection> map = UserAnswerTask.getConnectionMap();
            for(Connection cn : map.values()){
                closeConnection(cn);
            }
        }

        if(answerPageThreadPool.getQueue().size() < 500){
            //当前阻塞队列中数据已经不多，说明很少有新增进来数据，则拉取一些新的用户，开始爬取这些用户的答案
            List<String> userTokenList = AbstractPageTask.getZhiHuDao().listUserTokenLimitNumOrderById(currentOffset, LIMIT);
            logger.info("load new user_token, list={}");
            for (String e : userTokenList){
                logger.info("new userToken={}", e);
            }
            currentOffset += LIMIT;//更新offset位置
            for (String userToken : userTokenList){
                String startUrl = String.format(Constants.USER_ANSWER_URL, userToken, 0);
                HttpRequestBase request = new HttpGet(startUrl);
                request.setHeader("authorization", "oauth " + getAuthorization());
                answerPageThreadPool.execute(new UserAnswerTask(request, true, userToken));
            }
        }
        double costTime = (System.currentTimeMillis() - startTime) / 1000.0;//单位s
        sudu_logger.debug("抓取速率：" + parseUserAnswerCount.get() / costTime + "个/s");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ThreadPoolExecutor getThreadPool() {
        return answerPageThreadPool;
    }

    public AtomicInteger getParseUserAnswerCount() {
        return parseUserAnswerCount;
    }
}
