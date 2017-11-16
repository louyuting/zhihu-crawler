package com.crawl.zhihu;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.crawl.core.httpclient.IHttpClient;
import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.crawl.zhihu.dao.ZhiHuDaoMysqlImpl;
import com.crawl.zhihu.task.GeneralPageTask;
import org.slf4j.Logger;

/**
 * httpclient的基础类
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/16
 */
abstract public class AbstractHttpClient implements IHttpClient{
    private static Logger logger =  Constants.ZHIHU_LOGGER;
    /**
     * request　header
     * 获取列表页时，必须带上
     */
    private final String authorization;

    AbstractHttpClient() {
        initDB();
        this.authorization = initAuthorization();
    }

    @Override
    public void initDB() {
        if(Config.dbEnable){
            ZhiHuDaoMysqlImpl.DBTablesInit();
        }
    }

    @Override
    public void startCrawl(){
        startCrawl(Config.startUserToken);
    }

    @Override
    abstract public void startCrawl(String userToken);


    /**
     * 初始化authorization
     *
     * @return authorization
     */
    private String initAuthorization(){
        logger.info("初始化authoriztion中...");
        String content = null;

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

    @Override
    public String getAuthorization() {
        return authorization;
    }

    @Override
    abstract public ThreadPoolExecutor getThreadPool();

    protected void closeConnection(Connection cn){
        try {
            if (cn != null && !cn.isClosed()){
                cn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
