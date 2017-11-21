package com.crawl.zhihu.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.crawl.core.util.Constants;
import com.crawl.zhihu.container.ContainerPool;
import com.crawl.zhihu.dao.CommonDao;
import com.crawl.zhihu.dao.UserAnswerDao;
import com.crawl.zhihu.entity.Answer;
import org.slf4j.Logger;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public class UserAnswerDaoImpl implements UserAnswerDao {
    private static Logger logger =  Constants.ZHIHU_LOGGER;
    private static final int TEXT_MAX_LENGTH = 30000;
     private CommonDao commonDao = ContainerPool.getCommonDao();

    @Override
    public boolean isExistUserInAnswer(Connection cn, String userToken) {
        String isContainUserSql = "select count(*) from answer WHERE user_token='" + userToken + "'";
        try {
            if(commonDao.isExistRecord(cn, isContainUserSql)){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isExistUserAnswer(Connection cn, String userToken, Integer answerId) {
        String isContainUserSql = "select count(*) from answer WHERE user_token='" + userToken + "' and answer_id='" + answerId + "'";
        try {
            if(commonDao.isExistRecord(cn, isContainUserSql)){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isExistAnswer(Connection cn, Integer answerId) {
        String isContainSql = "select count(*) from answer WHERE answer_id='" + answerId + "'";
        try {
            if(commonDao.isExistRecord(cn, isContainSql)){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertAnswer(Connection cn, Answer answer) {
        try {
            if (isExistAnswer(cn, answer.getAnswerId())){
                logger.info("current answerIs is existed, answerId={}", answer.getAnswerId());
                return false;
            }
            String column = "comment_count,voteup_count,content,excerpt,created_time,updated_time,answer_id,question_id,question_title," +
                    "answer_url,user_token";
            String values = "?,?,?,?,?,?,?,?,?,?,?";
            String sql = "insert into answer (" + column + ") values(" +values+")";
            PreparedStatement pstmt;
            pstmt = cn.prepareStatement(sql);
            pstmt.setInt(1,answer.getCommentCount());
            pstmt.setInt(2,answer.getVoteupCount());
            if(answer.getContent().length() > TEXT_MAX_LENGTH){
                answer.setContent(answer.getContent().substring(0, TEXT_MAX_LENGTH));
            }
            if(answer.getExcerpt().length() > TEXT_MAX_LENGTH){
                answer.setExcerpt(answer.getExcerpt().substring(0, TEXT_MAX_LENGTH));
            }
            pstmt.setString(3,answer.getContent());
            pstmt.setString(4,answer.getExcerpt());
            pstmt.setInt(5, answer.getCreatedTime());
            pstmt.setInt(6, answer.getUpdatedTime());
            pstmt.setInt(7,answer.getAnswerId());
            pstmt.setInt(8,answer.getQuestionId());
            pstmt.setString(9,answer.getQuestionTitle());
            pstmt.setString(10,answer.getAnswerUrl());
            pstmt.setString(11,answer.getUserToken());
            pstmt.executeUpdate();
            pstmt.close();
            logger.info("插入数据库成功---"+ answer.getUserToken()  + "----" +answer.getQuestionId() + "----" +answer.getAnswerId() + "----" + answer.getQuestionTitle());
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("insert answer error, answer={}", answer);
            return false;
        }
        return true;
    }
}
