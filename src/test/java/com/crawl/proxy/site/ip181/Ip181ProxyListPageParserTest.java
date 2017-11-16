package com.crawl.proxy.site.ip181;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.crawl.proxy.entity.Proxy;
import com.crawl.zhihu.CommonHttpClientUtils;
import com.crawl.zhihu.entity.Page;
import org.junit.Test;

public class Ip181ProxyListPageParserTest {
    @Test
    public void testParse() throws IOException {
        System.out.println(Charset.defaultCharset().toString());
        //Page page = ProxyHttpClient.getInstance().getWebPage("http://www.ip181.com/daili/1.html");
        Page page = CommonHttpClientUtils.getWebPage("http://www.ip181.com/daili/1.html", "gb2312");
        List<Proxy> urlList = new Ip181ProxyListPageParser().parse(page.getHtml());
        System.out.println(urlList.size());
    }
}
