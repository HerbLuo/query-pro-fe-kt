package cn.cloudself.java;

import cn.cloudself.java.helpers.Helpers;
import cn.cloudself.java.helpers.query.User;
import cn.cloudself.java.helpers.query.UserQueryPro;
import cn.cloudself.query.QueryProConfig;
import cn.cloudself.query.QueryProSql;
import org.intellij.lang.annotations.Language;
import org.junit.Test;

import java.util.List;

import static cn.cloudself.helpers.HelpersKt.*;

public class SelectTest {
    private void prepareData() {
        @Language("SQL")
        String sql = "TRUNCATE TABLE user;" +
                "INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);" +
                "INSERT INTO user (id, name, age) VALUES (2, 'hb', 10);" +
                "INSERT INTO user (id, name, age) VALUES (3, 'herb', 18);" +
                "INSERT INTO user (id, name, age) VALUES (4, 'l', null);" +
                "TRUNCATE TABLE setting;" +
                "INSERT INTO setting (id, user_id, kee, value) VALUES (1, 1, 'lang', '简体中文');" +
                "INSERT INTO setting (id, user_id, kee, value) VALUES (2, 1, 'theme', 'dark');" +
                "INSERT INTO setting (id, user_id, kee, value) VALUES (3, 2, 'lang', '繁体中文');";
        QueryProSql.createBatchBySqlGroup(sql).update(Boolean.class);
    }

    @Test
    public void test() {
        initLogger();
        QueryProConfig.INSTANCE.setBeautifySql(false);
        QueryProConfig.INSTANCE.setLogicDelete(false);
        QueryProConfig.INSTANCE.setDataSource(getDataSource());

        prepareData();

        User user1 = new User().setId(1L).setName("hb").setAge(18);

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ?", Helpers.listOf(1));
        final List<User> usersRun1 = UserQueryPro.selectBy().id().equalsTo(1).run();
        assertEqualsForJava(usersRun1, Helpers.listOf(user1));


    }
}
