package com.crawl.zhihu.dao;

import java.sql.SQLException;
import java.util.List;

import com.crawl.core.db.ConnectionManager;
import com.crawl.zhihu.entity.Answer;
import org.junit.Assert;
import org.junit.Test;

public class ZhihuDAOTest {

    private static int answerId = 1234565432;

    @Test
    public void testDBTablesInit(){
        ZhiHuDaoMysqlImpl.DBTablesInit();
        ZhiHuDao zhiHuDao = new ZhiHuDaoMysqlImpl();
        List<String> res = zhiHuDao.listUserTokenLimitNumOrderById(1, 5);
        Assert.assertTrue(res.size() == 5);
        System.out.println(res);
    }

    @Test
    public void insertAnswer_test_normal_01() throws SQLException {
        ZhiHuDaoMysqlImpl.DBTablesInit();
        ZhiHuDao zhiHuDao = new ZhiHuDaoMysqlImpl();
        Answer answer = new Answer();
        answer.setCommentCount(20);
        answer.setVoteupCount(20);
        answer.setCreatedTime(123543213);
        answer.setUpdatedTime(333333333);
        answer.setContent("<p>答案</p>");
        answer.setExcerpt("答案");
        answer.setAnswerUrl("http://dnidn");
        answer.setAnswerId(answerId);
        answer.setQuestionId(1345434444);
        answer.setQuestionTitle("title");
        answer.setUserToken("louyuting");
        ConnectionManager.getConnection().setAutoCommit(false);
        boolean flag = zhiHuDao.insertAnswer(ConnectionManager.getConnection(), answer);
        ConnectionManager.getConnection().rollback();
        Assert.assertTrue(flag);
    }

    @Test
    public void isExistAnswer_test_normal_01(){
        ZhiHuDaoMysqlImpl.DBTablesInit();
        ZhiHuDao zhiHuDao = new ZhiHuDaoMysqlImpl();
        boolean flag = zhiHuDao.isExistAnswer(ConnectionManager.getConnection(), answerId);
        Assert.assertTrue(flag);
    }
}
