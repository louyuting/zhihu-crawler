package com.crawl.zhihu.container;

import java.lang.reflect.InvocationHandler;

import com.crawl.core.util.SimpleInvocationHandler;
import com.crawl.zhihu.ZhiHuUserAnswerHttpClient;
import com.crawl.zhihu.ZhiHuUserHttpClient;
import com.crawl.zhihu.dao.CommonDao;
import com.crawl.zhihu.dao.UrlDao;
import com.crawl.zhihu.dao.UserAnswerDao;
import com.crawl.zhihu.dao.UserDao;
import com.crawl.zhihu.dao.impl.CommonDaoImpl;
import com.crawl.zhihu.dao.impl.UrlDaoImpl;
import com.crawl.zhihu.dao.impl.UserAnswerDaoImpl;
import com.crawl.zhihu.dao.impl.UserDaoImpl;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/21
 */
public class ContainerPool {
    /**
     * 知乎的DAO对象
     */
    private static ZhiHuUserAnswerHttpClient userAnswerHttpClient;
    private static ZhiHuUserHttpClient userHttpClient;
    private static CommonDao commonDao;
    private static UserDao userDao;
    private static UrlDao urlDao;
    private static UserAnswerDao userAnswerDao;


    /**
     * 初始化容器，主要是实例化一些全局的单例对象
     */
    public static void initContainer(){
        commonDao = newCommonDaoInstanceProxy();
        userDao = newUserDaoInstanceProxy();
        urlDao = newUrlDaoInstanceProxy();
        userAnswerDao = newUserAnswerDaoInstanceProxy();
        // 顺序不可改变，下面客户端的初始化依赖于DAO的初始化
        userAnswerHttpClient = ZhiHuUserAnswerHttpClient.getInstance();
        userHttpClient = ZhiHuUserHttpClient.getInstance();
    }

    public static CommonDao getCommonDao() {
        return commonDao;
    }

    public static UserDao getUserDao() {
        return userDao;
    }

    public static UrlDao getUrlDao() {
        return urlDao;
    }

    public static UserAnswerDao getUserAnswerDao() {
        return userAnswerDao;
    }

    public static ZhiHuUserAnswerHttpClient getUserAnswerHttpClient() {
        return userAnswerHttpClient;
    }

    public static ZhiHuUserHttpClient getUserHttpClient() {
        return userHttpClient;
    }

    /**
	 * 代理DAO类，统计方法执行时间
	 * @return
	 */
	private static CommonDao newCommonDaoInstanceProxy(){
		CommonDao commonDao = new CommonDaoImpl();
		InvocationHandler invocationHandler = new SimpleInvocationHandler(commonDao);
		return (CommonDao) java.lang.reflect.Proxy.newProxyInstance(commonDao.getClass().getClassLoader(),
				commonDao.getClass().getInterfaces(), invocationHandler);
	}

	private static UserDao newUserDaoInstanceProxy(){
		UserDao userDao = new UserDaoImpl();
		InvocationHandler invocationHandler = new SimpleInvocationHandler(userDao);
		return (UserDao) java.lang.reflect.Proxy.newProxyInstance(userDao.getClass().getClassLoader(),
				userDao.getClass().getInterfaces(), invocationHandler);
	}

	private static UserAnswerDao newUserAnswerDaoInstanceProxy(){
		UserAnswerDao userAnswerDao = new UserAnswerDaoImpl();
		InvocationHandler invocationHandler = new SimpleInvocationHandler(userAnswerDao);
		return (UserAnswerDao) java.lang.reflect.Proxy.newProxyInstance(userAnswerDao.getClass().getClassLoader(),
				userAnswerDao.getClass().getInterfaces(), invocationHandler);
	}

	private static UrlDao newUrlDaoInstanceProxy(){
		UrlDao urlDao = new UrlDaoImpl();
		InvocationHandler invocationHandler = new SimpleInvocationHandler(urlDao);
		return  (UrlDao) java.lang.reflect.Proxy.newProxyInstance(urlDao.getClass().getClassLoader(),
				urlDao.getClass().getInterfaces(), invocationHandler);
	}
}
