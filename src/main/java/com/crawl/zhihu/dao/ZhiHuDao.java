package com.crawl.zhihu.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.crawl.zhihu.entity.Answer;
import com.crawl.zhihu.entity.User;

public interface ZhiHuDao {
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

    /**
     * 判断当前user是否已经存在
     * <p>注意：这里用的是全局的连接对象，此方法使用需要小心。</p>
     *
     * @param userToken
     * @return
     */
    boolean isExistUser(String userToken);

    /**
     * 判断当前用户是否已经存入user表
     *
     * @param cn
     * @param userToken
     * @return
     */
    boolean isExistUser(Connection cn, String userToken);

    /**
     * 插入一个user
     * <p>注意：这里用的是全局的连接对象，此方法使用需要小心。</p>
     *
     * @param user
     * @return
     */
    boolean insertUser(User user);

    /**
     * <p>使用指定的连接对象，插入用户数据。</p>
     *
     * @param cn
     * @param user
     * @return
     */
    boolean insertUser(Connection cn, User user);


    /**
     * <p>查询出指定数量的 user_token </p>
     * <p>注意：这里用的是全局的连接对象，此方法使用需要小心。</p>
     *
     * @param offset 查询起始位置
     * @param limit 查询记录数
     * @return user_token list
     */
    List<String> listUserTokenLimitNumOrderById(int offset, int limit);

    /**
     * <p>查询出指定数量的  user_token </p>
     *
     * @param offset 查询起始位置
     * @param limit 查询记录数
     * @return user_token list
     */
    List<String> listUserTokenLimitNumOrderById(Connection cn, int offset, int limit);


    /**
     * 插入url,插入成功返回true，若已存在该url则返回false
     * @param cn
     * @param md5Url
     * @return
     */
    boolean insertUrl(Connection cn, String md5Url);

    /**
     * 判断当前用户是否已经在answer表中
     *
     * @param cn
     * @param userToken
     * @return
     */
    boolean isExistUserInAnswer(Connection cn, String userToken);

    /**
     * 判断当前user的当前答案是否已经解析过了。
     *
     * @param cn
     * @param userToken
     * @param answerId
     * @return
     */
    boolean isExistUserAnswer(Connection cn, String userToken, Integer answerId);

    /**
     * 判断当前answer是都已经解析过了
     *
     * @param cn
     * @param answerId
     * @return
     */
    boolean isExistAnswer(Connection cn, Integer answerId);
    /**
     * 插入一条 Answer记录
     * @param cn
     * @param answer
     * @return
     */
    boolean insertAnswer(Connection cn, Answer answer);
}
