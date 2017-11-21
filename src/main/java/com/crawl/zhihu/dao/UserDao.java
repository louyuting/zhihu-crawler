package com.crawl.zhihu.dao;

import java.sql.Connection;
import java.util.List;

import com.crawl.zhihu.entity.User;

/**
 * 用户相关的DAO
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public interface UserDao {
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
     * <p>根据主键id从user表查询出当前id对应的记录的user_token</p>
     *
     * @param id
     * @return 如果当前id对应的记录存在，就返回userToken
     */
    String getUserTokenById(long id);

    /**
     * <p>根据主键id从user表查询出当前id对应的记录的user_token</p>
     *
     * @param id
     * @return 如果当前id对应的记录存在，就返回userToken
     */
    String getUserTokenById(Connection cn, long id);

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
}
