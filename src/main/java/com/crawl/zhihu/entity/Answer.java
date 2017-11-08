package com.crawl.zhihu.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 用户回答的某个问题的答案
 */
public class Answer {
    /**
     * 评论人数
     */
    private Integer commentCount;
    /**
     * 点赞数
     */
    private Integer voteupCount;
    /**
     * 答案（富文本）
     */
    private String content;
    /**
     * 答案
     */
    private String excerpt;
    /**
     * 答案创建时间
     */
    private Integer createdTime;
    /**
     * 更新时间
     */
    private Integer updatedTime;
    /**
     * answer id
     */
    private Integer answerId;
    /**
     * questionId
     */
    private Integer questionId;
    /**
     * question title
     */
    private String questionTitle;
    /**
     * 答案url
     */
    private String answerUrl;
    /**
     * 用户唯一标识
     */
    private String userToken;

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public Integer getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Integer createdTime) {
        this.createdTime = createdTime;
    }

    public Integer getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Integer updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getAnswerUrl() {
        return answerUrl;
    }

    public void setAnswerUrl(String answerUrl) {
        this.answerUrl = answerUrl;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public Integer getVoteupCount() {
        return voteupCount;
    }

    public void setVoteupCount(Integer voteupCount) {
        this.voteupCount = voteupCount;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
