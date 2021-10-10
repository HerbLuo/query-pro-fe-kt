package cn.cloudself

import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.User
import cn.cloudself.helpers.query.UserQueryPro
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import org.junit.Test
import kotlin.test.assertContentEquals

class InsertTest {
    private fun prepareData() {
        QueryProSql.create("TRUNCATE table user").update()
    }

    @Test
    fun test() {
        initLogger()

        QueryProConfig.global.setBeautifySql(false)
        QueryProConfig.global.setDataSource(getDataSource())

        prepareData()

        UserQueryPro.insert(User(name = "hb", age = 18)).also { assert(it == 1L) }

        UserQueryPro.insert(User(name = "hb2", age = 18)).also { assert(it == 2L) }

        UserQueryPro.insert(
            User(name = "hb", age = 19),
            User(name = "hb", age = 20),
            User(7, "hb", 21),
        ).also { assertContentEquals(it, listOf(3L, 4L, 7L)) }
    }
}