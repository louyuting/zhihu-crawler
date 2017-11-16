package com.crawl.proxy.site.mimiip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.crawl.proxy.entity.Proxy;
import com.crawl.zhihu.CommonHttpClientUtils;
import com.crawl.zhihu.entity.Page;
import org.junit.Test;

public class MimiipProxyListPageParserTest {
    @Test
    public void testParse() throws IOException {
        System.out.println(Charset.defaultCharset().toString());
        Page page = CommonHttpClientUtils.getWebPage("http://www.mimiip.com/gngao/1");
        List<Proxy> urlList = new MimiipProxyListPageParser().parse(page.getHtml());
        System.out.println(urlList.size());
    }
}
