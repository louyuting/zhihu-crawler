package com.crawl.core.httpclient;

import java.util.concurrent.ThreadPoolExecutor;

public interface IHttpClient {
    /**
     * 初始化DB客户端
     */
    void initDB();

    /**
     * 爬虫入口，使用默认的userToken
     */
    void startCrawl();

    /**
     * 爬虫入口，指定起始用户
     * @param userToken
     */
    void startCrawl(String userToken);

    /**
     * 获取当前IP的oauth认证的token
     *
     * @return
     */
    String getAuthorization();

    /**
     * 获取当前爬虫绑定的线程池
     * @return
     */
    ThreadPoolExecutor getThreadPool();
}
