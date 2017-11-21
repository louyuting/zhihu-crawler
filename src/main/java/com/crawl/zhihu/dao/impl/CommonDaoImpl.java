package com.crawl.zhihu.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.crawl.core.db.ConnectionManager;
import com.crawl.core.util.Constants;
import com.crawl.zhihu.dao.CommonDao;
import org.slf4j.Logger;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public class CommonDaoImpl implements CommonDao {
    private static Logger logger =  Constants.ZHIHU_LOGGER;

    /**
     * 数据库表初始化
     */
    public static void DBTablesInit() {
        ResultSet rs = null;
        Properties p = new Properties();
        Connection cn = ConnectionManager.getConnection();
        try {
            //加载properties文件
            p.load(CommonDaoImpl.class.getResourceAsStream("/config.properties"));
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
        } catch (Exception e) {
            Constants.MONITOR_LOGGER.error("initDB error! exception type is {}", e.getClass().getName());
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
}
