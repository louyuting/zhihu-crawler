package com.crawl.zhihu.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 问题的信息
 *
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/6
 */
public class Question {
    /**
     * question id
     */
    private Integer questionId;
    /**
     * 创建时间
     */
    private Date createdTime;
    /**
     * 更新时间
     */
    private Date updatedTime;
    /**
     * 问题类型
     */
    private String questionType;
    /**
     * 问题名称
     */
    private String title;
    /**
     * 问题URL
     */
    private String questionUrl;
    /**
     * 类型
     */
    private String type;

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestionUrl() {
        return questionUrl;
    }

    public void setQuestionUrl(String questionUrl) {
        this.questionUrl = questionUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
