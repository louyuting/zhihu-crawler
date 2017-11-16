package com.crawl.proxy.site.xicidaili;

import java.io.IOException;
import java.util.List;

import com.crawl.core.util.HttpClientUtil;
import com.crawl.proxy.entity.Proxy;
import com.crawl.zhihu.CommonHttpClientUtils;
import com.crawl.zhihu.entity.Page;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

public class XicidailiProxyListPageParserTest {
    @Test
    public void testParse() throws IOException {
        RequestConfig.Builder requestBuilder = HttpClientUtil.getRequestConfigBuilder();
        requestBuilder.setProxy(new HttpHost("139.59.72.185", 8080));
        HttpGet request = new HttpGet("http://www.xicidaili.com/wt/1.html");
        request.setConfig(requestBuilder.build());
        Page page = CommonHttpClientUtils.getWebPage(request);
        List<Proxy> urlList = new XicidailiProxyListPageParser().parse(page.getHtml());
        System.out.println(urlList.size());
    }
}
