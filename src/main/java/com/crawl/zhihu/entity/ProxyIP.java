package com.crawl.zhihu.entity;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 代理IP对象
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/16
 */
public class ProxyIP {
    /**
     * 主键id
     */
    private Long id;
    /**
     * IP:端口
     */
    private String ip;
    /**
     * 上次使用时间
     */
    private Date lastUseTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 测试IP返回的时间
     */
    private Double responseTime;
    /**
     * 免费代理IP来源网站
     */
    private String source;
    /**
     * 此代理爬取的目标地址
     */
    private String targetUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(Date lastUseTime) {
        this.lastUseTime = lastUseTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Double responseTime) {
        this.responseTime = responseTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
