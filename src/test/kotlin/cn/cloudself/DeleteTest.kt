package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.query.UserQueryPro
import cn.cloudself.query.QueryProConfig
import org.junit.Test

class DeleteTest {
    @Test
    fun test() {
        QueryProConfig.dryRun = true
        QueryProConfig.beautifySql = false

        expectSqlResult("DELETE FROM `user` WHERE `user`.`id` = ?", listOf(1))
        val success: Boolean = UserQueryPro.deleteBy().id.equalsTo(1).run()
    }
}