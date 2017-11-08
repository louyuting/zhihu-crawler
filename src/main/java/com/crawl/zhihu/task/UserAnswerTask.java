package com.crawl.zhihu.task;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.crawl.core.db.ConnectionManager;
import com.crawl.core.parser.UserAnswerPageParser;
import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.core.util.SimpleInvocationHandler;
import com.crawl.zhihu.ZhiHuHttpClient;
import com.crawl.zhihu.entity.Answer;
import com.crawl.zhihu.entity.Page;
import com.crawl.zhihu.parser.ZhiHuUserAnswerListPageParser;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

/**
 * 爬取用户回答的答案的Task
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/6
 */
public class UserAnswerTask extends AbstractPageTask{

    private static Logger logger =  Constants.ZHIHU_LOGGER;
    private static UserAnswerPageParser proxyUserAnswerPageParser;
    private String userToken;

    /**
     * <p>Thread-数据库连接对象 的Map</p>
     * <li>这里维护了一个数据库连接池</li>
     * <li>里面保存的连接对象数量与线程池里面数量有关系.</li>
     */
    private static Map<Thread, Connection> connectionMap = new ConcurrentHashMap<>();

    static {
        proxyUserAnswerPageParser = getProxyUserAnswerPageParser();
    }

    public UserAnswerTask(){

    }

    public UserAnswerTask(HttpRequestBase request, boolean proxyFlag, String userToken){
        super(request, proxyFlag);
        this.userToken = userToken;
    }

    /**
     * 代理类
     * @return
     */
    private static UserAnswerPageParser getProxyUserAnswerPageParser(){
        UserAnswerPageParser userAnswerPageParser = ZhiHuUserAnswerListPageParser.getInstance();
        InvocationHandler invocationHandler = new SimpleInvocationHandler(userAnswerPageParser);
        UserAnswerPageParser proxyUserAnswerPageParser = (UserAnswerPageParser) Proxy.newProxyInstance(
            userAnswerPageParser.getClass().getClassLoader(),
                userAnswerPageParser.getClass().getInterfaces(), invocationHandler);
        return proxyUserAnswerPageParser;
    }



    @Override
    void retry() {
        zhiHuHttpClient.getAnswerPageThreadPool().execute(new UserAnswerTask(request, true, this.userToken));
    }

    @Override
    void handle(Page page) {
        List<Answer> answerList = proxyUserAnswerPageParser.parseAnswerListPage(page);
        for(Answer answer : answerList){
            logger.info("解析answer成功: "+ answer.toString());
            if(Config.dbEnable){
                Connection cn = getConnection();
                // 判断当前用户是否已经解析过了
                if(zhiHuDao.isExistUserAnswer(cn, this.userToken, answer.getAnswerId())){
                    logger.info("current answer has parsed, answer={}", answer);
                    continue;
                }
                if (zhiHuDao.insertAnswer(cn, answer)){
                    ZhiHuHttpClient.getParseUserAnswerCount().incrementAndGet();
                } else {
                    logger.error("insert answer fail!, answer={}", answer);
                }
            }
        }

        DocumentContext dc = JsonPath.parse(page.getHtml());
        boolean isStart = dc.read("$.paging.is_start");
        if (isStart){
            Integer totals = dc.read("$.paging.totals");
            for (int j = 1; j < totals; j++) {
                String nextUrl = String.format(Constants.USER_ANSWER_URL, userToken, j * 20);
                HttpRequestBase request = new HttpGet(nextUrl);
                request.setHeader("authorization", "oauth " + ZhiHuHttpClient.getAuthorization());
                zhiHuHttpClient.getAnswerPageThreadPool().execute(new UserAnswerTask(request, true, userToken));
            }
        }
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每个thread维护一个Connection
     * @return
     */
    private Connection getConnection(){
        Thread currentThread = Thread.currentThread();
        Connection cn = null;
        if (!connectionMap.containsKey(currentThread)){
            cn = ConnectionManager.createConnection();
            connectionMap.put(currentThread, cn);
        }  else {
            cn = connectionMap.get(currentThread);
        }
        return cn;
    }

    public static Map<Thread, Connection> getConnectionMap() {
        return connectionMap;
    }

}
