package com.crawl.zhihu.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.crawl.core.util.Constants;
import com.crawl.zhihu.container.ContainerPool;
import com.crawl.zhihu.dao.CommonDao;
import com.crawl.zhihu.dao.UrlDao;
import org.slf4j.Logger;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public class UrlDaoImpl implements UrlDao {
    private static Logger logger =  Constants.ZHIHU_LOGGER;
    private CommonDao commonDao = ContainerPool.getCommonDao();

    @Override
    public boolean insertUrl(Connection cn, String md5Url) {
        String isContainSql = "select count(*) from url WHERE md5_url ='" + md5Url + "'";
        try {
            if(commonDao.isExistRecord(cn, isContainSql)){
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

}
