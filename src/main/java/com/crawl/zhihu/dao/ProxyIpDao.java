package com.crawl.zhihu.dao;

import java.sql.Connection;

import com.crawl.zhihu.entity.ProxyIP;

/**
 * 代理IP持久化的接口
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/16
 */
public interface ProxyIpDao {

    /**
     * 判断当前IP和端口是否已经持久化了。
     *
     * @param ip
     * @return
     */
    boolean isExistProxyIP(Connection cn, String ip);

    /**
     * 判断当前IP和端口是否已经持久化了。
     *
     * @param ip
     * @return
     */
    boolean isExistProxyIP(String ip);

    /**
     * 插入一条IP记录
     *
     * @param cn
     * @param proxyIP
     * @return
     */
    boolean insertProxyIp(Connection cn, ProxyIP proxyIP);

    /**
     * 插入一条IP记录
     *
     * @param proxyIP
     * @return
     */
    boolean insertProxyIp(ProxyIP proxyIP);
}
