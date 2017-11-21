package com.crawl.zhihu.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 一些公共操作，不感知具体业务的DAO
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public interface CommonDao {
    /**
     * 判断当前sql执行的操作记录是否已经存在
     * <p>注意：这里用的是全局的连接对象，此方法使用需要小心。</p>
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    boolean isExistRecord(String sql) throws SQLException;

    /**
     * <p>判断当前sql执行的操作记录是否已经存在</p>
     *
     * @param cn
     * @param sql
     * @return
     * @throws SQLException
     */
    boolean isExistRecord(Connection cn, String sql) throws SQLException;
}
