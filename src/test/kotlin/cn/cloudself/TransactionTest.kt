package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.*
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import cn.cloudself.query.QueryProTransaction
import org.junit.Test
import kotlin.test.assertEquals

class TransactionTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE user;
                INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);
                INSERT INTO user (id, name, age) VALUES (2, 'hb', 10);
                INSERT INTO user (id, name, age) VALUES (3, 'herb', 18);
                INSERT INTO user (id, name, age) VALUES (4, 'l', null);
                TRUNCATE TABLE setting;
                INSERT INTO setting (id, user_id, kee, value) VALUES (1, 1, 'lang', '简体中文');
                INSERT INTO setting (id, user_id, kee, value) VALUES (2, 1, 'theme', 'dark');
                INSERT INTO setting (id, user_id, kee, value) VALUES (3, 2, 'lang', '繁体中文');
            """
        ).update(Boolean::class.java)
    }

    @Test
    fun test() {
        initLogger()

        QueryProConfig.global.setBeautifySql(false)
        QueryProConfig.global.setLogicDelete(false)
        QueryProConfig.global.setDataSource(getDataSource())

        prepareData()

        val user1 = User(1, "hb", 18)
        val user2 = User(2, "hb", 10)
        val user3 = User(3, "herb", 18)
        val user4 = User(4, "l", null)

        QueryProTransaction.use {
            expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ?", listOf(1))
            UserQueryPro.selectBy().id.equalsTo(1).run()
                .also { users: List<User> -> assertEquals(users, listOf(user1)) }
        }
    }
}