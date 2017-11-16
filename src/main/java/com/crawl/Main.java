package com.crawl;

import com.crawl.zhihu.ZhiHuUserHttpClient;

/**
 * 爬虫入口
 */
public class Main {
    public static void main(String args []){
        ZhiHuUserHttpClient.getInstance().startCrawl();
    }
}
