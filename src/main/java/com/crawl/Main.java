package com.crawl;

import com.crawl.core.util.Constants;
import com.crawl.zhihu.ZhiHuUserAnswerHttpClient;
import org.slf4j.Logger;

/**
 * 爬虫入口
 */
public class Main {
    private static Logger logger = Constants.ZHIHU_LOGGER;

    public static void main(String args []){
        logger.info("参数长度是：{}", args.length);
        logger.info("参数列表是：");
        for (String arg : args){
            logger.info(arg);
        }
        if(args.length > 0){
            String startToken = String.valueOf(args[0]);
            ZhiHuUserAnswerHttpClient.getInstance().startCrawl(startToken);
        }else {
            ZhiHuUserAnswerHttpClient.getInstance().startCrawl();
        }
    }
}
