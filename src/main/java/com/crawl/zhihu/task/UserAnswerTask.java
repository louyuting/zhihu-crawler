package com.crawl.zhihu.task;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.crawl.core.db.ConnectionManager;
import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.zhihu.ZhiHuHttpClient;
import com.crawl.zhihu.entity.Answer;
import com.crawl.zhihu.entity.Page;
import com.google.common.collect.Lists;
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
    private String userToken;

    /**
     * <p>Thread-数据库连接对象 的Map</p>
     * <li>这里维护了一个数据库连接池</li>
     * <li>里面保存的连接对象数量与线程池里面数量有关系.</li>
     */
    private static Map<Thread, Connection> connectionMap = new ConcurrentHashMap<>();

    public UserAnswerTask(){

    }

    /**
     *
     * @param request
     * @param proxyFlag
     * @param userToken
     */
    public UserAnswerTask(HttpRequestBase request, boolean proxyFlag, String userToken){
        super(request, proxyFlag);
        this.userToken = userToken;
    }

    @Override
    void retry() {
        zhiHuHttpClient.getAnswerPageThreadPool().execute(new UserAnswerTask(request, true, this.userToken));
    }

    @Override
    void handle(Page page) {
        DocumentContext dc = JsonPath.parse(page.getHtml());
        List<Answer> answerList = parseAnswers(dc);
        for(Answer answer : answerList){
            logger.info("解析answer成功: "+ answer.toString());
            if(Config.dbEnable){
                Connection cn = getConnection();
                // 判断当前用户是否已经解析过了
                /*if(zhiHuDao.isExistUserInAnswer(cn, this.userToken)){
                    return;
                }*/
                if (zhiHuDao.insertAnswer(cn, answer)){
                    zhiHuHttpClient.getParseUserAnswerCount().incrementAndGet();
                } else {
                    logger.error("insert answer fail!, answer={}", answer);
                }
            }
        }
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

    }

    @Override
    void releaseConnection() {

    }



    private List<Answer> parseAnswers(DocumentContext dc){
        List<Answer> answerList = Lists.newArrayList();
        try {
            int answerCount = dc.read("$.data.length()");
            for(int i = 0; i < answerCount; i++){
                Answer answer = new Answer();
                Integer commentCount = dc.read("$.data[" + i +"].comment_count");
                Integer voteupCount  = dc.read("$.data[" + i +"].voteup_count");
                String content = dc.read("$.data[" + i + "].content");
                String excerpt = dc.read("$.data[" + i + "].excerpt");
                Date createdTime = new Date(((Integer)dc.read("$.data[" + i + "].created_time")).longValue());
                Date updatedTime = new Date(((Integer)dc.read("$.data[" + i + "].updated_time")).longValue());
                Integer answerId = dc.read("$.data[" + i + "].id");
                Integer questionId = dc.read("$.data[" + i + "].question.id");
                String questionTitle = dc.read("$.data[" + i + "].question.title");
                String answerUrl = "https://www.zhihu.com/question/%s/answer/%s";
                answerUrl = String.format(answerUrl, questionId, answerId);
                String userToken = dc.read("$.data[" + i + "].author.url_token");

                answer.setCommentCount(commentCount);
                answer.setVoteupCount(voteupCount);
                answer.setContent(content);
                answer.setExcerpt(excerpt);
                answer.setCreatedTime(createdTime);
                answer.setUpdatedTime(updatedTime);
                answer.setAnswerId(answerId);
                answer.setQuestionId(questionId);
                answer.setQuestionTitle(questionTitle);
                answer.setAnswerUrl(answerUrl);
                answer.setUserToken(userToken);
                answerList.add(answer);
            }
        } catch (Throwable e) {
            logger.error("com.crawl.zhihu.task.UserAnswerTask.parseAnswers error! param={}", dc.toString());
        }
        return answerList;
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
