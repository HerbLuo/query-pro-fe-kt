package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.*
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import cn.cloudself.query.QueryProTransaction
import org.junit.Test
import java.lang.RuntimeException
import kotlin.test.assertEquals

class TransactionTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE user;
                INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);
                INSERT INTO user (id, name, age) VALUES (2, 'herb', 19);
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

        UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 18, false)), users) }

        try {
            QueryProTransaction.use {
                expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(19, 1))
                UserQueryPro.updateSet(User(age = 19)).where.id.equalsTo(1).run().also { assert(it) }
                UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 19, false)), users) }

                expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(20, 2))
                UserQueryPro.updateSet(User(age = 20)).where.id.equalsTo(2).run().also { assert(it) }
                UserQueryPro.selectBy().id.equalsTo(2).run().also { users: List<User> -> assertEquals(listOf(User(2, "herb", 20, false)), users) }

                expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(20, 1))
                UserQueryPro.updateSet(User(age = 20)).where.id.equalsTo(1).run().also { assert(it) }
                UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 20, false)), users) }

                throw RuntimeException("test")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 18, false)), users) }
        UserQueryPro.selectBy().id.equalsTo(2).run().also { users: List<User> -> assertEquals(listOf(User(2, "herb", 19, false)), users) }

        QueryProTransaction.use {
            expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(19, 1))
            UserQueryPro.updateSet(User(age = 19)).where.id.equalsTo(1).run().also { assert(it) }
            UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 19, false)), users) }

            expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(20, 2))
            UserQueryPro.updateSet(User(age = 20)).where.id.equalsTo(2).run().also { assert(it) }
            UserQueryPro.selectBy().id.equalsTo(2).run().also { users: List<User> -> assertEquals(listOf(User(2, "herb", 20, false)), users) }

            expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(20, 1))
            UserQueryPro.updateSet(User(age = 20)).where.id.equalsTo(1).run().also { assert(it) }
            UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 20, false)), users) }
        }


        UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 20, false)), users) }
        UserQueryPro.selectBy().id.equalsTo(2).run().also { users: List<User> -> assertEquals(listOf(User(2, "herb", 20, false)), users) }
    }
}
