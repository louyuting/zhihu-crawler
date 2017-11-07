package com.crawl.zhihu;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crawl.core.httpclient.AbstractHttpClient;
import com.crawl.core.httpclient.IHttpClient;
import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.core.util.SimpleThreadPoolExecutor;
import com.crawl.core.util.ThreadPoolMonitor;
import com.crawl.proxy.ProxyHttpClient;
import com.crawl.zhihu.dao.ZhiHuDaoMysqlImpl;
import com.crawl.zhihu.task.DetailListPageTask;
import com.crawl.zhihu.task.GeneralPageTask;
import com.crawl.zhihu.task.UserAnswerTask;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

public class ZhiHuHttpClient extends AbstractHttpClient implements IHttpClient {
    private static Logger logger =  Constants.ZHIHU_LOGGER;
    private volatile static ZhiHuHttpClient instance;
    /**
     * 统计用户数量
     */
    public static AtomicInteger parseUserCount = new AtomicInteger(0);
    /**
     * 统计用户answer数量
     */
    public static AtomicInteger parseUserAnswerCount = new AtomicInteger(0);
    private static long startTime = System.currentTimeMillis();
    public static volatile boolean isStop = false;

    public static ZhiHuHttpClient getInstance(){
        if (instance == null){
            synchronized (ZhiHuHttpClient.class){
                if (instance == null){
                    instance = new ZhiHuHttpClient();
                }
            }
        }
        return instance;
    }
    /**
     * 详情列表页下载线程池
     */
    private ThreadPoolExecutor detailListPageThreadPool;
    /**
     * 答案页下载线程池
     */
    private ThreadPoolExecutor answerPageThreadPool;


    /**
     * request　header
     * 获取列表页时，必须带上
     */
    private static String authorization;
    private ZhiHuHttpClient() {
        initDB();
        initThreadPool();
    }
    /**
     * 初始化HttpClient
     */
    @Override
    public void initDB() {
        if(Config.dbEnable){
            ZhiHuDaoMysqlImpl.DBTablesInit();
        }
    }

    /**
     * 初始化线程池
     */
    private void initThreadPool(){
        detailListPageThreadPool = new SimpleThreadPoolExecutor(Config.downloadThreadSize,
                Config.downloadThreadSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                new ThreadPoolExecutor.DiscardPolicy(),
                "detailListPageThreadPool");
        new Thread(new ThreadPoolMonitor(detailListPageThreadPool, "DetailListPageThreadPool")).start();
    }

    @Override
    public void startCrawl() {
        authorization = initAuthorization();

        String startToken = Config.startUserToken;
        String startUrl = String.format(Constants.USER_FOLLOWEES_URL, startToken, 0);
        HttpGet request = new HttpGet(startUrl);
        request.setHeader("authorization", "oauth " + ZhiHuHttpClient.getAuthorization());
        detailListPageThreadPool.execute(new DetailListPageTask(request, Config.isProxy));
        manageHttpClient();
    }


    public void startCrawlAnswer(String userToken){
        answerPageThreadPool = new SimpleThreadPoolExecutor(Config.downloadThreadSize,
                Config.downloadThreadSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                new ThreadPoolExecutor.DiscardPolicy(),
                "answerPageThreadPool");
        new Thread(new ThreadPoolMonitor(answerPageThreadPool, "AnswerPageThreadPool")).start();
        if(StringUtils.isBlank(authorization)){
            authorization = initAuthorization();
        }
        String startUrl = String.format(Constants.USER_ANSWER_URL, userToken, 0);
        HttpRequestBase request = new HttpGet(startUrl);
        request.setHeader("authorization", "oauth " + ZhiHuHttpClient.getAuthorization());
        answerPageThreadPool.execute(new UserAnswerTask(request, true, userToken));
    }


    /**
     * 初始化authorization
     * @return
     */
    private String initAuthorization(){
        logger.info("初始化authoriztion中...");
        String content = null;

//            content = HttpClientUtil.getWebPage(Config.startURL);
        GeneralPageTask generalPageTask = new GeneralPageTask(Config.startURL, true);
        generalPageTask.run();
        content = generalPageTask.getPage().getHtml();

        Pattern pattern = Pattern.compile("https://static\\.zhihu\\.com/heifetz/main\\.app\\.([0-9]|[a-z])*\\.js");
        Matcher matcher = pattern.matcher(content);
        String jsSrc = null;
        if (matcher.find()){
            jsSrc = matcher.group(0);
        } else {
            throw new RuntimeException("not find javascript url");
        }
        String jsContent = null;
//        try {
//            jsContent = HttpClientUtil.getWebPage(jsSrc);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        GeneralPageTask jsPageTask = new GeneralPageTask(jsSrc, true);
        jsPageTask.run();
        jsContent = jsPageTask.getPage().getHtml();

        pattern = Pattern.compile("oauth\\\"\\),h=\\\"(([0-9]|[a-z])*)\"");
        matcher = pattern.matcher(jsContent);
        if (matcher.find()){
            String authorization = matcher.group(1);
            logger.info("初始化authoriztion完成");
            return authorization;
        }
        throw new RuntimeException("not get authorization");
    }
    public static String getAuthorization(){
        return authorization;
    }
    /**
     * 管理知乎客户端
     * 关闭整个爬虫
     */
    public void manageHttpClient(){
        while (true) {
            /**
             * 下载网页数
             */
            long downloadPageCount = detailListPageThreadPool.getTaskCount();

            if (downloadPageCount >= Config.downloadPageCount &&
                    !detailListPageThreadPool.isShutdown()) {
                isStop = true;
                ThreadPoolMonitor.setIsStopMonitor(true);
                detailListPageThreadPool.shutdown();
            }
            if(detailListPageThreadPool.isTerminated()){
                //关闭数据库连接
                Map<Thread, Connection> map = DetailListPageTask.getConnectionMap();
                for(Connection cn : map.values()){
                    try {
                        if (cn != null && !cn.isClosed()){
                            cn.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                //关闭代理检测线程池
                ProxyHttpClient.getInstance().getProxyTestThreadExecutor().shutdownNow();
                //关闭代理下载页线程池
                ProxyHttpClient.getInstance().getProxyDownloadThreadExecutor().shutdownNow();

                break;
            }
            double costTime = (System.currentTimeMillis() - startTime) / 1000.0;//单位s
            logger.debug("抓取速率：" + parseUserCount.get() / costTime + "个/s");
//            logger.info("downloadFailureProxyPageSet size:" + ProxyHttpClient.downloadFailureProxyPageSet.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public ThreadPoolExecutor getDetailListPageThreadPool() {
        return detailListPageThreadPool;
    }

    public ThreadPoolExecutor getAnswerPageThreadPool() {
        return answerPageThreadPool;
    }

    public static AtomicInteger getParseUserAnswerCount() {
        return parseUserAnswerCount;
    }
}
