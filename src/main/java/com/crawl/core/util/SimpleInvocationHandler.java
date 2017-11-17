package com.crawl.core.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;

/**
 * 动态代理
 */
public class SimpleInvocationHandler implements InvocationHandler{
    private static Logger logger = Constants.DAO_LOGGER;

    private Object target;

    public SimpleInvocationHandler() {
        super();
    }

    public SimpleInvocationHandler(Object target) {
        super();
        this.target = target;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = method.invoke(target, args);
        long endTime = System.currentTimeMillis();
        logger.debug(target.getClass().getSimpleName() + " " + method.getName() + " cost time:" + (endTime - startTime) + "ms");
        return result;
    }
}
