package com.crawl.zhihu.parser;

import java.util.Date;
import java.util.List;

import com.crawl.core.parser.UserAnswerPageParser;
import com.crawl.core.util.Constants;
import com.crawl.zhihu.ZhiHuHttpClient;
import com.crawl.zhihu.entity.Answer;
import com.crawl.zhihu.entity.Page;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/8
 */
public class ZhiHuUserAnswerListPageParser implements UserAnswerPageParser {

    private static ZhiHuUserAnswerListPageParser instance;
    private static Logger logger =  Constants.ZHIHU_LOGGER;

    public static ZhiHuUserAnswerListPageParser getInstance(){
        if (instance == null){
            synchronized (ZhiHuHttpClient.class){
                if (instance == null){
                    instance = new ZhiHuUserAnswerListPageParser();
                }
            }
        }
        return instance;
    }

    @Override
    public List<Answer> parseAnswerListPage(Page page) {
        DocumentContext dc = JsonPath.parse(page.getHtml());
        return parseAnswers(dc);
    }



    private List<Answer> parseAnswers(DocumentContext dc){
        List<Answer> answerList = Lists.newArrayList();
        try {
            int answerCount = dc.read("$.data.length()");
            for(int i = 0; i < answerCount; i++){
                Answer answer = new Answer();
                Integer commentCount = dc.read("$.data[" + i +"].comment_count");
                Integer voteupCount  = dc.read("$.data[" + i +"].voteup_count");
                String content = dc.read("$.data[" + i + "].content");
                String excerpt = dc.read("$.data[" + i + "].excerpt");
                Integer createdTime = dc.read("$.data[" + i + "].created_time");
                Integer updatedTime = dc.read("$.data[" + i + "].updated_time");
                Integer answerId = dc.read("$.data[" + i + "].id");
                Integer questionId = dc.read("$.data[" + i + "].question.id");
                String questionTitle = dc.read("$.data[" + i + "].question.title");
                String answerUrl = "https://www.zhihu.com/question/%s/answer/%s";
                answerUrl = String.format(answerUrl, questionId, answerId);
                String userToken = dc.read("$.data[" + i + "].author.url_token");

                answer.setCommentCount(commentCount);
                answer.setVoteupCount(voteupCount);
                answer.setContent(content);
                answer.setExcerpt(excerpt);
                answer.setCreatedTime(createdTime);
                answer.setUpdatedTime(updatedTime);
                answer.setAnswerId(answerId);
                answer.setQuestionId(questionId);
                answer.setQuestionTitle(questionTitle);
                answer.setAnswerUrl(answerUrl);
                answer.setUserToken(userToken);
                answerList.add(answer);
            }
        } catch (Throwable e) {
            logger.error("com.crawl.zhihu.task.UserAnswerTask.parseAnswers error! param={}", dc.toString());
        }
        return answerList;
    }

}
