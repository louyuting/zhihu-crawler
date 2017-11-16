package com.crawl.zhihu;

import java.io.IOException;

import com.crawl.core.util.HttpClientUtil;
import com.crawl.zhihu.entity.Page;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

/**
 * @author 惜暮
 * @email chris.lyt@cainiao.com
 * @date 2017/11/16
 */
public class CommonHttpClientUtils {

    public static Page getWebPage(String url) throws IOException {
        return getWebPage(url, "UTF-8");
    }

    public static Page getWebPage(String url, String charset) throws IOException {
        Page page = new Page();
        CloseableHttpResponse response = null;
        response = HttpClientUtil.getResponse(url);
        page.setStatusCode(response.getStatusLine().getStatusCode());
        page.setUrl(url);
        try {
            if(page.getStatusCode() == 200){
                page.setHtml(EntityUtils.toString(response.getEntity(), charset));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return page;
    }

    public static Page getWebPage(HttpRequestBase request, String charset) throws IOException {
        CloseableHttpResponse response = null;
            response = HttpClientUtil.getResponse(request);
            Page page = new Page();
            page.setStatusCode(response.getStatusLine().getStatusCode());
            page.setHtml(EntityUtils.toString(response.getEntity(),charset));
            page.setUrl(request.getURI().toString());
            return page;
    }

    public static Page getWebPage(HttpRequestBase request) throws IOException {
        CloseableHttpResponse response = null;
            response = HttpClientUtil.getResponse(request);
            Page page = new Page();
            page.setStatusCode(response.getStatusLine().getStatusCode());
            page.setHtml(EntityUtils.toString(response.getEntity()));
            page.setUrl(request.getURI().toString());
            return page;
    }
}
