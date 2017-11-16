package com.crawl;

import com.crawl.zhihu.ZhiHuUserHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 爬虫入口
 */
public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String args []){
        //ProxyHttpClient.getInstance().startCrawl();
        //ZhiHuUserHttpClient.getInstance().startCrawl(Config.startUserToken);
        ZhiHuUserHttpClient.getInstance().startCrawl();
    }
}
