package com.crawl.zhihu.dao;

import java.sql.Connection;

import com.crawl.zhihu.entity.Answer;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public interface UserAnswerDao {

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
