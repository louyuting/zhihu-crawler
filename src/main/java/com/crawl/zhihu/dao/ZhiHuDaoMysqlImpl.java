package com.crawl.zhihu.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawl.core.db.ConnectionManager;
import com.crawl.core.util.Constants;
import com.crawl.zhihu.entity.Answer;
import com.crawl.zhihu.entity.User;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

/**
 * 数据访问层， MySQL实现
 *
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/10/17
 */
public class ZhiHuDaoMysqlImpl implements ZhiHuDao {
    private static Logger logger =  Constants.ZHIHU_LOGGER;

    private static final int spinCount = 30;
    private static AtomicInteger currentCount = new AtomicInteger(0);

    /**
     * 数据库表初始化
     */
    public static void DBTablesInit() {
        ResultSet rs = null;
        Properties p = new Properties();
        Connection cn = ConnectionManager.getConnection();
        try {
            //加载properties文件
            p.load(ZhiHuDaoMysqlImpl.class.getResourceAsStream("/config.properties"));
             /** check url 表 */
            rs = cn.getMetaData().getTables(null, null, "url", null);
            Statement st = cn.createStatement();
            //不存在url表
            if(!rs.next()){
                //创建url表
                st.execute(p.getProperty("createUrlTable"));
                logger.info("url表创建成功");
                //st.execute(p.getProperty("createUrlIndex"));
                //logger.info("url表索引创建成功");
            }
            else{
                logger.info("url表已存在");
            }

            /** check user 表 */
            rs = cn.getMetaData().getTables(null, null, "user", null);
            //不存在user表
            if(!rs.next()){
                //创建user表
                st.execute(p.getProperty("createUserTable"));
                logger.info("user表创建成功");
                //st.execute(p.getProperty("createUserIndex"));
                //logger.info("user表索引创建成功");
            }
            else{
                logger.info("user表已存在");
            }

             /** check answer 表 */
            rs = cn.getMetaData().getTables(null, null, "answer", null);
            //不存在user表
            if(!rs.next()){
                //创建user表
                st.execute(p.getProperty("createUserAnswer"));
                logger.info("answer表创建成功");
            }
            else{
                logger.info("answer表已存在");
            }
            rs.close();
            st.close();
            cn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isExistRecord(String sql) throws SQLException{
        return isExistRecord(ConnectionManager.getConnection(), sql);
    }

    @Override
    public boolean isExistRecord(Connection cn, String sql) throws SQLException {
        int num = 0;
        PreparedStatement pstmt;
        pstmt = cn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()){
            num = rs.getInt("count(*)");
        }
        rs.close();
        pstmt.close();
        if(num == 0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public boolean isExistUser(String userToken) {
        return isExistUser(ConnectionManager.getConnection(), userToken);
    }

    @Override
    public boolean isExistUser(Connection cn, String userToken) {
        String isContainSql = "select count(*) from user WHERE user_token='" + userToken + "'";
        try {
            if(isExistRecord(isContainSql)){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insertUser(User u) {
        return insertUser(ConnectionManager.getConnection(), u);
    }

    @Override
    public boolean insertUser(Connection cn, User u) {
        try {
            if (isExistUser(cn, u.getUserToken())){
                return false;
            }
            String column = "location,business,sex,employment,username,url,agrees,thanks,asks," +
                    "answers,posts,followees,followers,hashId,education,user_token";
            String values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
            String sql = "insert into user (" + column + ") values(" +values+")";
            PreparedStatement pstmt;
            pstmt = cn.prepareStatement(sql);
            pstmt.setString(1,u.getLocation());
            pstmt.setString(2,u.getBusiness());
            pstmt.setString(3,u.getSex());
            pstmt.setString(4,u.getEmployment());
            pstmt.setString(5,u.getUsername());
            pstmt.setString(6,u.getUrl());
            pstmt.setInt(7,u.getAgrees());
            pstmt.setInt(8,u.getThanks());
            pstmt.setInt(9,u.getAsks());
            pstmt.setInt(10,u.getAnswers());
            pstmt.setInt(11,u.getPosts());
            pstmt.setInt(12,u.getFollowees());
            pstmt.setInt(13,u.getFollowers());
            pstmt.setString(14,u.getHashId());
            pstmt.setString(15,u.getEducation());
            pstmt.setString(16,u.getUserToken());
            pstmt.executeUpdate();
            pstmt.close();
            logger.info("插入数据库成功---" + u.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
//            ConnectionManager.close();
        }
        return true;
    }

    @Override
    public List<String> listUserTokenLimitNumOrderById(int offset, int limit) {
        return listUserTokenLimitNumOrderById(ConnectionManager.getConnection(), offset, limit);
    }

    @Override
    public List<String> listUserTokenLimitNumOrderById(Connection cn, int offset, int limit) {
        // 自旋10次获取数据，只要获取到就成功并返回，否则继续自旋获取
        // 防止offset不存在的情况
        for (;;){
            currentCount.incrementAndGet();
            String sql = "select user_token from user order by id asc limit " + offset + "," + limit;
            List<String> res = Lists.newArrayList();
            try {
                PreparedStatement pstmt;
                pstmt = cn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()){
                    res.add(rs.getString("user_token"));
                }
                return res;
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("com.crawl.zhihu.dao.ZhiHuDaoMysqlImpl.listUserTokenLimitNumOrderById error! offset={}, limit={}", offset, limit);
                offset ++;
            }
            // 当超过自旋次数就直接返回一个空的list
            if(currentCount.get() >= spinCount){
                return Lists.newArrayList();
            }
        }
    }

    @Override
    public boolean insertUrl(Connection cn, String md5Url) {
        String isContainSql = "select count(*) from url WHERE md5_url ='" + md5Url + "'";
        try {
            if(isExistRecord(cn, isContainSql)){
                logger.debug("数据库已经存在该url---" + md5Url);
                return false;
            }
            String sql = "insert into url (md5_url) values( ?)";
            PreparedStatement pstmt;
            pstmt = cn.prepareStatement(sql);
            pstmt.setString(1,md5Url);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.debug("url插入成功---");
        return true;
    }


    @Override
    public boolean isExistUserInAnswer(Connection cn, String userToken, Integer answerId) {
        String isContainUserSql = "select count(*) from answer WHERE user_token='" + userToken + "' and answer_id='" + answerId + "'";
        try {
            if(isExistRecord(cn, isContainUserSql)){
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
            if(isExistRecord(cn, isContainSql)){
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
            pstmt.setString(3,answer.getContent());
            pstmt.setString(4,answer.getExcerpt());
            pstmt.setDate(5,new Date(answer.getCreatedTime().getTime()));
            pstmt.setDate(6,new Date(answer.getUpdatedTime().getTime()));
            pstmt.setInt(7,answer.getAnswerId());
            pstmt.setInt(8,answer.getQuestionId());
            pstmt.setString(9,answer.getQuestionTitle());
            pstmt.setString(10,answer.getAnswerUrl());
            pstmt.setString(11,answer.getUserToken());
            pstmt.executeUpdate();
            pstmt.close();
            logger.info("插入数据库成功---" + answer.getQuestionTitle() + "----" +answer.getExcerpt());
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("insert answer error, answer={}", answer);
            return false;
        }
        return true;
    }
}
