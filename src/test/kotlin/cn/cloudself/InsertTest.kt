package cn.cloudself

import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.query.User
import cn.cloudself.helpers.query.UserQueryPro
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import org.junit.Test

class InsertTest {
    private fun prepareData() {
        QueryProSql.create("TRUNCATE table user").update()
    }

    @Test
    fun test() {
        QueryProConfig.beautifySql = false
        QueryProConfig.setDataSource(getDataSource())

        prepareData()

        UserQueryPro.insert(User(name = "hb", age = 18)).also { assert(it == 1) }

        UserQueryPro.insert(
            User(name = "hb", age = 19),
            User(name = "hb", age = 20),
            User(5, "hb", 21),
        )
    }
}