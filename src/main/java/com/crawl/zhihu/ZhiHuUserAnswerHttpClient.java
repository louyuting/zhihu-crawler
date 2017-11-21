package com.crawl.zhihu;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.core.util.SimpleThreadFactory;
import com.crawl.core.util.SimpleThreadPoolExecutor;
import com.crawl.core.util.ThreadPoolMonitor;
import com.crawl.core.util.TimeDelayUtil;
import com.crawl.zhihu.container.ContainerPool;
import com.crawl.zhihu.dao.UserAnswerDao;
import com.crawl.zhihu.dao.UserDao;
import com.crawl.zhihu.task.UserAnswerTask;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

/**
 * 爬取用户回答答案的爬虫客户端
 */
public class ZhiHuUserAnswerHttpClient extends AbstractHttpClient {
    private static Logger logger =  Constants.ZHIHU_LOGGER;
    private static Logger sudu_logger =  Constants.SUDU_LOGGER;
    private volatile static ZhiHuUserAnswerHttpClient instance;
    /**
     * 统计用户answer数量
     */
    private static AtomicInteger parseUserAnswerCount = new AtomicInteger(0);
    private static volatile boolean isStop = false;
    /** 数据库相关的参数 */
    private static int currentOffset = 1;
    private static final int LIMIT = 10;
    /**
     * 答案页下载线程池
     */
    private static ThreadPoolExecutor answerPageThreadPool;
    /** 线程池名称 */
    private static final String THREAD_POOL_NAME = "answerPageThreadPool";

    private static UserDao userDao = ContainerPool.getUserDao();
    private static UserAnswerDao userAnswerDao = ContainerPool.getUserAnswerDao();


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

    private ZhiHuUserAnswerHttpClient() {
        super();//init: DB、authorization
        initCurrentOffset();
        initAnswerThreadPool();
    }

    private static void initCurrentOffset(){
        //根据数据库实际情况初始化 currentOffset
        boolean lastestOffset = true;
        logger.info("开始校正user表，获取user_token没被爬取答案，且其id最小。");
        while (lastestOffset){
            String userTokenTemp = userDao.getUserTokenById(UserAnswerTask.getConnection(), currentOffset);
            if (StringUtils.isBlank(userTokenTemp)){
                continue;
            }
            //todo 这里存在多台服务器，刚好同时竞争情况，考虑分布式锁
            lastestOffset = userAnswerDao.isExistUserInAnswer(UserAnswerTask.getConnection(), userTokenTemp);
            if(!lastestOffset){
                break;
            }
            currentOffset++;
        }
        logger.info("user表校正成功，currentOffset={}", currentOffset);
    }

    private static void initAnswerThreadPool(){
        answerPageThreadPool = new SimpleThreadPoolExecutor(Config.downloadUserAnswerThreadSize, Config.downloadUserAnswerThreadSize,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10000), new SimpleThreadFactory(THREAD_POOL_NAME),
                new ThreadPoolExecutor.DiscardPolicy(), THREAD_POOL_NAME);
    }

    @Override
    public void startCrawl(String userToken){
        new Thread(new ThreadPoolMonitor(answerPageThreadPool, "AnswerPageThreadPool"), "monitorThread").start();
        String startUrl = String.format(Constants.USER_ANSWER_URL, userToken, 0);
        HttpRequestBase request = new HttpGet(startUrl);
        request.setHeader("authorization", "oauth " + getAuthorization());
        answerPageThreadPool.execute(new UserAnswerTask(request, true, userToken));
        manageHttpClient();
    }

    /**
     * 管理知乎客户端
     * 关闭整个爬虫
     */
    private void manageHttpClient(){
        try {
            Thread.sleep(20000);
            while (true) {
                manageUserAnswerThreadPool();
                if(answerPageThreadPool.isTerminated() && isStop){
                    break;
                }
            }
        } catch (Exception e) {
            Constants.MONITOR_LOGGER.error("manageHttpClient error!");
            e.printStackTrace();
        }
    }

    private void manageUserAnswerThreadPool(){
        long startTime = System.currentTimeMillis();
        int before = parseUserAnswerCount.get();
        /**
         * 下载网页数
         */
        long downloadPageCount = answerPageThreadPool.getTaskCount();
        if (downloadPageCount >= Config.downloadUserAnswerPageCount && !answerPageThreadPool.isShutdown()) {
            isStop = true;
            ThreadPoolMonitor.setIsStopMonitor(true);
            answerPageThreadPool.shutdown();
            return;
        }
        if(answerPageThreadPool.isTerminated()){
            //关闭所有线程绑定的数据库连接
            Map<Thread, Connection> map = UserAnswerTask.getConnectionMap();
            for(Entry<Thread, Connection> entry : map.entrySet()){
                if (entry.getKey() != Thread.currentThread()){
                    //不是主线程，就关闭连接对象
                    closeConnection(entry.getValue());
                }
            }
        }
        if(answerPageThreadPool.getQueue().size() < 100){
            addNewUserToken2ThreadPoolTask();
        }
        // 等待10s
        TimeDelayUtil.delayMilli(10000);
        double costTime = (System.currentTimeMillis() - startTime) / 1000.0;
        sudu_logger.debug("抓取速率：" + (parseUserAnswerCount.get() - before)/ costTime + "个/s");
    }

    /**
     * 当线程池中任务队列数量到达一个比较少的阈值，就添加新的用户到线程池进行拉取
     */
    private void addNewUserToken2ThreadPoolTask(){
        //自旋
        int count = 0;
        List<String> res = Lists.newArrayList();
        for (;;){
            count++;
            List<String> userTokenList = userDao.listUserTokenLimitNumOrderById(UserAnswerTask.getConnection(), currentOffset, LIMIT);
            if(userTokenList.size() == 0){
                currentOffset += LIMIT;
                continue;
            } else {
                res.addAll(userTokenList);
            }

            if (res.size()>=20 || count>1000){
                count = 0;
                break;
            }
        }
        logger.info("load new user_token, list is:");
        for (String e : res){
            logger.info("new userToken={}", e);
        }
        currentOffset += LIMIT;//更新offset位置
        for (String userToken : res){
            String startUrl = String.format(Constants.USER_ANSWER_URL, userToken, 0);
            HttpRequestBase request = new HttpGet(startUrl);
            request.setHeader("authorization", "oauth " + getAuthorization());
            answerPageThreadPool.execute(new UserAnswerTask(request, false, userToken));
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
