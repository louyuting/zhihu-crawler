package com.crawl.zhihu.task;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;

import com.crawl.core.util.Constants;
import com.crawl.core.util.SimpleInvocationHandler;
import com.crawl.zhihu.CommonHttpClientUtils;
import com.crawl.zhihu.dao.ZhiHuDao;
import com.crawl.zhihu.dao.ZhiHuDaoMysqlImpl;
import com.crawl.zhihu.entity.Page;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

/**
 * page task
 * 下载网页并解析，具体解析由子类实现
 * 若使用代理，从ProxyPool中取
 */
public abstract class AbstractPageTask implements Runnable{
	private static Logger logger =  Constants.ZHIHU_LOGGER;
	protected String url;//当前task需要爬取数据的请求的URL
	protected HttpRequestBase request; //当前task需要爬取数据的请求
	protected boolean proxyFlag;//是否通过代理下载
	protected static ZhiHuDao zhiHuDao;
	static {
		zhiHuDao = newZhiHuDaoInstanceProxy();
	}
	public AbstractPageTask(){

	}
	public AbstractPageTask(String url, boolean proxyFlag){
		this.url = url;
		this.proxyFlag = proxyFlag;
	}
	public AbstractPageTask(HttpRequestBase request, boolean proxyFlag){
		this.request = request;
		this.proxyFlag = proxyFlag;
	}
	public void run(){
		long requestStartTime = 0l;
		HttpGet tempRequest = null;
		try {
			Page page = null;
			if(url != null){
				if (proxyFlag){
					tempRequest = new HttpGet(url);
					requestStartTime = System.currentTimeMillis();
					page = CommonHttpClientUtils.getWebPage(tempRequest);
				}else {
					requestStartTime = System.currentTimeMillis();
					page = CommonHttpClientUtils.getWebPage(url);
				}
			} else if(request != null){
				if (proxyFlag){
					requestStartTime = System.currentTimeMillis();
					page = CommonHttpClientUtils.getWebPage(request);
				}else {
					requestStartTime = System.currentTimeMillis();
					page = CommonHttpClientUtils.getWebPage(request);
				}
			}
			long requestEndTime = System.currentTimeMillis();
			int status = page.getStatusCode();
			String logStr = Thread.currentThread().getName() + " " +
					"  executing request " + page.getUrl()  + " response statusCode:" + status +
					"  request cost time:" + (requestEndTime - requestStartTime) + "ms";
			if(status == HttpStatus.SC_OK){
				if (page.getHtml().contains("zhihu") && !page.getHtml().contains("安全验证")){
					logger.debug(logStr);
					handle(page);
				}else {
				}
			}
			/**
			 * 401--不能通过验证
			 */
			else if(status == 404 || status == 401 ||
					status == 410){
				logger.warn(logStr);
			}
			else {
				logger.error(logStr);
				Thread.sleep(100);
				retry();
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException", e);
		} catch (IOException e) {
            retry();
		} finally {
			if (request != null){
				request.releaseConnection();
			}
			if (tempRequest != null){
				tempRequest.releaseConnection();
			}
		}
	}

	/**
	 * retry
	 */
	abstract void retry();

	/**
	 * 子类实现对page的处理
	 * @param page
	 */
	abstract void handle(Page page);

	/**
	 * 代理DAO类，统计方法执行时间
	 * @return
	 */
	private static ZhiHuDao newZhiHuDaoInstanceProxy(){
		ZhiHuDao zhiHuDao = new ZhiHuDaoMysqlImpl();
		InvocationHandler invocationHandler = new SimpleInvocationHandler(zhiHuDao);
		ZhiHuDao proxyZhiHuDao = (ZhiHuDao) java.lang.reflect.Proxy.newProxyInstance(zhiHuDao.getClass().getClassLoader(),
				zhiHuDao.getClass().getInterfaces(), invocationHandler);
		return proxyZhiHuDao;
	}

    public static ZhiHuDao getZhiHuDao() {
        return zhiHuDao;
    }
}
