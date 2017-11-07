package com.crawl.proxy;

import java.util.List;

import com.crawl.core.parser.Parser;
import com.crawl.proxy.entity.Proxy;

public interface ProxyListPageParser extends Parser{
    /**
     * 是否只要匿名代理
     */
    static final boolean anonymousFlag = true;
    List<Proxy> parse(String content);
}
