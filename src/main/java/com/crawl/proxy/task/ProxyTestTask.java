package com.crawl.proxy.task;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawl.core.util.Constants;
import com.crawl.proxy.ProxyPool;
import com.crawl.proxy.entity.Proxy;
import com.crawl.zhihu.CommonHttpClientUtils;
import com.crawl.zhihu.entity.Page;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;

/**
 * 代理检测task
 * 通过访问知乎首页，能否正确响应
 * 将可用代理添加到DelayQueue延时队列中
 */
public class ProxyTestTask implements Runnable{
    private final static Logger logger = Constants.PROXY_LOGGER;
    private Proxy proxy;

    private static AtomicInteger total = new AtomicInteger(0);
    private static AtomicInteger success = new AtomicInteger(0);


    public ProxyTestTask(Proxy proxy){
        this.proxy = proxy;
    }
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        HttpGet request = new HttpGet(Constants.INDEX_URL);
        try {
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(Constants.TIMEOUT).
                    setConnectTimeout(Constants.TIMEOUT).
                    setConnectionRequestTimeout(Constants.TIMEOUT).
                    setProxy(new HttpHost(proxy.getIp(), proxy.getPort())).
                    setCookieSpec(CookieSpecs.STANDARD).
                    build();
            request.setConfig(requestConfig);
            Page page = CommonHttpClientUtils.getWebPage(request);
            long endTime = System.currentTimeMillis();
            String logStr = Thread.currentThread().getName() + " " + proxy.getProxyStr() +
                    "  executing request " + page.getUrl()  + " response statusCode:" + page.getStatusCode() +
                    "  request cost time:" + (endTime - startTime) + "ms";

            total.incrementAndGet();
            logger.info("success={}", success.intValue());
            logger.info("total={}", total.intValue());
            if (page == null || page.getStatusCode() != 200){
                logger.warn(logStr);
                logger.info("success/total={}", success.doubleValue()/total.intValue());
                return;
            }
            logger.info("success/total={}", success.doubleValue()/total.intValue());
            success.incrementAndGet();
            request.releaseConnection();
            logger.debug(proxy.toString() + "----------proxy-is-available--------use time:" + (endTime - startTime) + "ms");
            ProxyPool.proxyQueue.add(proxy);
        } catch (IOException e) {
            /**
             * ConnectTimeoutException
             *
             * failed: connect timed out
             *
             */
            logger.debug("IOException:", e);
        } finally {
            if (request != null){
                request.releaseConnection();
            }
        }
    }
    private String getProxyStr(){
        return proxy.getIp() + ":" + proxy.getPort();
    }
}
