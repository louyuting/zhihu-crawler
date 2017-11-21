package com.crawl.zhihu.dao;

import java.sql.Connection;

/**
 * URL相关的DAO
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public interface UrlDao {
    /**
     * 插入url,插入成功返回true，若已存在该url则返回false
     * @param cn
     * @param md5Url
     * @return
     */
    boolean insertUrl(Connection cn, String md5Url);
}
