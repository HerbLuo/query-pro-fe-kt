package cn.cloudself

import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.query.User
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import org.junit.Test
import kotlin.test.assertContentEquals

class QueryProSqlTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE user;
                INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);
                INSERT INTO user (id, name, age) VALUES (2, 'hb', 10);
            """
        ).update(Boolean::class.java)
    }

    @Test
    fun test() {
        QueryProConfig.beautifySql = false
        QueryProConfig.setDataSource(getDataSource())

        prepareData()

        QueryProSql.create("SELECT * FROM user").query(User::class.java)
            .also { users: List<User> -> assertContentEquals(
                users,
                listOf(User(1, "hb", 18), User(2, "hb", 10))
            ) }

    }
}