package com.crawl.core.httpclient;

import java.util.concurrent.ThreadPoolExecutor;

public interface IHttpClient {
    /**
     * 初始化DB客户端
     */
    void initDB();

    /**
     * 爬虫入口，使用config.properties里面默认的userToken
     */
    void startCrawl();

    /**
     * 爬虫入口，指定起始用户
     *
     * @param userToken 默认的userToken
     */
    void startCrawl(String userToken);

    /**
     * 爬虫入口
     *
     * @param userTokenList 指定的多个起始用户 userToken
     */
    void startCrawl(String[] userTokenList);

    /**
     * 获取当前IP的oauth认证的token
     *
     * @return
     */
    String getAuthorization();

    /**
     * 获取当前爬虫绑定的线程池
     *
     * @return
     */
    ThreadPoolExecutor getThreadPool();
}
