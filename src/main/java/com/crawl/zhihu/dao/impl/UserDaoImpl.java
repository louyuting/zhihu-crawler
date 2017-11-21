package com.crawl.zhihu.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawl.core.db.ConnectionManager;
import com.crawl.core.util.Constants;
import com.crawl.zhihu.container.ContainerPool;
import com.crawl.zhihu.dao.CommonDao;
import com.crawl.zhihu.dao.UserAnswerDao;
import com.crawl.zhihu.dao.UserDao;
import com.crawl.zhihu.entity.User;
import com.google.common.collect.Lists;
import org.slf4j.Logger;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public class UserDaoImpl implements UserDao {
    private static Logger logger =  Constants.ZHIHU_LOGGER;
    private static final int spinMax = 30;
    private static AtomicInteger currentCount = new AtomicInteger(0);
    private CommonDao commonDao = ContainerPool.getCommonDao();
    private UserAnswerDao userAnswerDao = ContainerPool.getUserAnswerDao();

    @Override
    public boolean isExistUser(String userToken) {
        return isExistUser(ConnectionManager.getConnection(), userToken);
    }

    @Override
    public boolean isExistUser(Connection cn, String userToken) {
        String isContainSql = "select count(*) from user WHERE user_token='" + userToken + "'";
        try {
            if(commonDao.isExistRecord(isContainSql)){
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
    public String getUserTokenById(long id) {
        return getUserTokenById(ConnectionManager.getConnection(), id);
    }

    @Override
    public String getUserTokenById(Connection cn, long id) {
        String sql = "select user_token from user where id=" + id;
         String res = null;
        try {
            PreparedStatement pstmt;
            pstmt = cn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                res = rs.getString("user_token");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
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
                List<String> existed = Lists.newArrayList();
                for(String userToken : res){
                    if (userAnswerDao.isExistUserInAnswer(cn, userToken)){
                        existed.add(userToken);
                    }
                }
                res.removeAll(existed);
                return res;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("com.crawl.zhihu.dao.ZhiHuDaoMysqlImpl.listUserTokenLimitNumOrderById error! offset={}, limit={}", offset, limit);
                offset ++;
                if((e instanceof SQLException) &&  e.toString().contains("Too many connections")){
                    offset--;
                }
            }
            // 当超过自旋次数就直接返回一个空的list
            if(currentCount.get() >= spinMax){
                return Lists.newArrayList();
            }
        }
    }
}
