package com.crawl.core.parser;

import java.util.List;

import com.crawl.zhihu.entity.Answer;
import com.crawl.zhihu.entity.Page;

/**
 * 用户回答答案解析器
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/8
 */
public interface UserAnswerPageParser extends Parser {
    List<Answer> parseAnswerListPage(Page page);
}
