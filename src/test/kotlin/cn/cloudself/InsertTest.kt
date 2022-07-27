package cn.cloudself

import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.User
import cn.cloudself.helpers.query.UserQueryPro
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import org.junit.Test
import kotlin.test.assertEquals

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

        UserQueryPro.insert(User(name = "hb", age = 18)).also { id: Long? -> assert(id == 1L) }

        UserQueryPro.insert(User(name = "hb2", age = 18)).also { id: Long? -> assert(id == 2L) }

        UserQueryPro.insert(
            User(name = "hb", age = 19),
            User(name = "hb", age = 20),
            User(7, "hb", 21),
        ).also { assertEquals(it, listOf(3L, 4L, 7L)) }

        UserQueryPro.insert(
            mapOf<String, Any?>("name" to "hb", "age" to 18),
            mapOf<String, Any?>("name" to "hb2", "age" to 18),
        ).also { assertEquals(it, listOf(8L, 9L)) }

        val users = mutableListOf<User>()
        for (i in 1..100000) {
            users.add(User(name = "u$i", age = 18))
        }
        UserQueryPro.insert(users)
    }
}