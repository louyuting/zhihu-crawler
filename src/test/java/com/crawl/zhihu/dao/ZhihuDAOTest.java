package com.crawl.zhihu.dao;

import java.sql.SQLException;
import java.util.List;

import com.crawl.core.db.ConnectionManager;
import com.crawl.zhihu.dao.impl.CommonDaoImpl;
import com.crawl.zhihu.dao.impl.UserAnswerDaoImpl;
import com.crawl.zhihu.dao.impl.UserDaoImpl;
import com.crawl.zhihu.entity.Answer;
import org.junit.Assert;
import org.junit.Test;

public class ZhihuDAOTest {

    private static int answerId = 1234565432;

    @Test
    public void testDBTablesInit(){
        CommonDaoImpl.DBTablesInit();
        UserDao userDao = new UserDaoImpl();
        List<String> res = userDao.listUserTokenLimitNumOrderById(1, 5);
        Assert.assertTrue(res.size() == 5);
        System.out.println(res);
    }

    @Test
    public void insertAnswer_test_normal_01() throws SQLException {
        CommonDaoImpl.DBTablesInit();
        UserAnswerDao userAnswerDao = new UserAnswerDaoImpl();
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
        boolean flag = userAnswerDao.insertAnswer(ConnectionManager.getConnection(), answer);
        ConnectionManager.getConnection().rollback();
        Assert.assertTrue(flag);
    }

}
