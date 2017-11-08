package com.crawl.core.parser;

import java.util.List;

import com.crawl.zhihu.entity.Page;

public interface UserListPageParser extends Parser {
    List parseListPage(Page page);
}
