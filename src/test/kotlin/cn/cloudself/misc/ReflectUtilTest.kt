package cn.cloudself.misc

import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.UserQueryPro
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.util.Reflect
import org.junit.Test
import kotlin.test.assertEquals

class ReflectUtilTest {
    @Test
    fun test() {
        initLogger()

        QueryProConfig.global.setBeautifySql(false)
        QueryProConfig.global.setDataSource(getDataSource())

        val user1 = Reflect.of(UserQueryPro)
            .invoke("selectBy")
            .invoke("id", 3L)
            .invoke("run")
            .getResult()

        val user2 = Reflect.of(UserQueryPro)
            .invoke("selectBy")
            .invoke("getId")
            .invoke("equalsTo", 3L)
            .invoke("run")
            .getResult()

        assertEquals(user1, user2)
    }
}