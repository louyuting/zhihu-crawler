package com.crawl;

import com.crawl.core.util.Config;
import com.crawl.zhihu.ZhiHuHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 爬虫入口
 */
public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String args []){
        //ProxyHttpClient.getInstance().startCrawl();
        //ZhiHuHttpClient.getInstance().startCrawl();
        ZhiHuHttpClient.getInstance().startCrawlAnswer(Config.startUserToken);
    }
}
