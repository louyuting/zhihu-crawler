package com.crawl.zhihu.task;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.crawl.core.db.ConnectionManager;
import com.crawl.core.parser.UserAnswerPageParser;
import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.core.util.SimpleInvocationHandler;
import com.crawl.core.util.TimeDelayUtil;
import com.crawl.zhihu.ZhiHuUserAnswerHttpClient;
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

    private static ZhiHuUserAnswerHttpClient zhiHuUserAnswerHttpClient = ZhiHuUserAnswerHttpClient.getInstance();

    static {
        proxyUserAnswerPageParser = getProxyUserAnswerPageParser();
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
        return (UserAnswerPageParser) Proxy.newProxyInstance(
            userAnswerPageParser.getClass().getClassLoader(), userAnswerPageParser.getClass().getInterfaces(), invocationHandler);
    }

    @Override
    void retry() {
        zhiHuUserAnswerHttpClient.getThreadPool().execute(new UserAnswerTask(request, false, this.userToken));
    }

    @Override
    void handle(Page page) {
        List<Answer> answerList = proxyUserAnswerPageParser.parseAnswerListPage(page);
        for(Answer answer : answerList){
            logger.info("answer解析成功---userToken={"+ this.userToken  + "}, questionId={" +answer.getQuestionId() + "}, answerId={" +
                answer.getAnswerId() + "}, questionTitle={" + answer.getQuestionTitle()+"}");
            if(Config.dbEnable){
                Connection cn = getConnection();
                // 判断当前用户的当前答案是否已经解析过了
                if(userAnswerDao.isExistUserAnswer(cn, this.userToken, answer.getAnswerId())){
                    logger.info(this.userToken + " current answer has parsed, answer={}", answer);
                    continue;
                }
                if (userAnswerDao.insertAnswer(cn, answer)){
                    zhiHuUserAnswerHttpClient.getParseUserAnswerCount().incrementAndGet();
                } else {
                    logger.error("insert answer fail!, answer={}", answer);
                }
            }
        }
        DocumentContext dc = JsonPath.parse(page.getHtml());
        boolean isStart = dc.read("$.paging.is_start");
        if (isStart){
            Integer totals = dc.read("$.paging.totals");
            int pageNum = (totals%20==0)? totals/20 : ((totals/20)+1);
            for (int j = 1; j <= pageNum; j++) {
                String nextUrl = String.format(Constants.USER_ANSWER_URL, userToken, j * 20);
                HttpRequestBase request = new HttpGet(nextUrl);
                request.setHeader("authorization", "oauth " + zhiHuUserAnswerHttpClient.getAuthorization());
                zhiHuUserAnswerHttpClient.getThreadPool().execute(new UserAnswerTask(request, true, userToken));
            }
        }
        try {
            TimeDelayUtil.delayMilli(666);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 每个thread维护一个Connection
     * @return
     */
    public static Connection getConnection(){
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
