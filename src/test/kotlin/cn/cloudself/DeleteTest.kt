package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.User
import cn.cloudself.helpers.query.UserQueryPro
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import org.junit.Test
import kotlin.test.assertEquals

class DeleteTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE user;
                INSERT INTO user (id, name, age) VALUES (1, 'delete-test', 18);
                INSERT INTO user (id, name, age) VALUES (2, 'delete-test', 10);
                INSERT INTO user (id, name, age) VALUES (3, 'delete-test2', 18);
                INSERT INTO user (id, name, age) VALUES (4, 'delete-test3', 18);
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

        UserQueryPro.selectBy().id.equalsTo(1).runLimit1()
            .also { user: User? -> assert(user != null) }

        expectSqlResult("DELETE FROM `user` WHERE `user`.`id` = ?", listOf(1))
        UserQueryPro.deleteBy().id.equalsTo(1).run()
            .also { success: Boolean -> assert(success) }

        UserQueryPro.selectBy().id.equalsTo(1).runLimit1()
            .also { user: User? -> assertEquals(user, null) }

        UserQueryPro.deleteByPrimaryKey(2)
            .also { success: Boolean -> assert(success) }
    }
}