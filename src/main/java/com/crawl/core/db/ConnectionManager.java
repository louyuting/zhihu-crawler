package com.crawl.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;

import com.crawl.core.util.Config;
import com.crawl.core.util.Constants;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

/**
 * DB Connection管理
 */
public class ConnectionManager {
	private static Logger logger =  Constants.ZHIHU_LOGGER;
	/** 静态的全局connection对象 */
	private static Connection conn;
	public static synchronized Connection getConnection(){
		//获取数据库连接
		try {
			if(conn == null || conn.isClosed()){
                conn = createConnection();
            }
            else{
                return conn;
            }
		} catch (SQLException e) {
			logger.error("SQLException",e);
		}
		return conn;
	}

	static {
		try {
			Class.forName("org.gjt.mm.mysql.Driver") ;//加载驱动
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static void close(){
		if(conn != null){
			//logger.info("关闭连接中");
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error("SQLException",e);
			}
		}
	}


    /**
     * 创建一个新的connection对象
     * @return
     */
	public static Connection createConnection(){
		String host = Config.dbHost;
		String user = Config.dbUsername;
		String password = Config.dbPassword;
		String dbName = Config.dbName;
		String url="jdbc:mysql://" + host + ":3306/" + dbName + "?characterEncoding=utf8";
		Connection con=null;
		try{
			con = DriverManager.getConnection(url,user,password);//建立mysql的连接
			logger.debug("success!");
		} catch(MySQLSyntaxErrorException e){
			logger.error("数据库不存在..请先手动创建创建数据库:" + dbName);
			e.printStackTrace();
		} catch(SQLException e2){
			logger.error("SQLException",e2);
		}
		return con;
	}
}
