package cn.cloudself

import cn.cloudself.query.*
import cn.cloudself.query.structure_reolsver.QueryStructureToSql
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class StructureToSqlTest {
    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testSelect() {
        QueryProConfig.dryRun = true
        QueryProConfig.beautifySql = false

        expectSqlResult("SELECT setting.id FROM setting WHERE setting.id = ?", listOf(1))
        val ids: List<Long?> = SettingQueryPro.selectBy().id.equalsTo(1).columnLimiter().id()

        expectSqlResult("SELECT * FROM user WHERE user.name = ? AND user.age = ?", listOf(1, 1000))
        val users1: List<User> = UserQueryPro.selectBy().name.`is`.equalsTo(1).and().age.`is`.equalsTo(1000).run()

        expectSqlResult("SELECT * FROM user WHERE user.id = ? or user.name in (?, ?, ?)", listOf(1, "Tom", "Cat", "Luo"))
        val users2: List<User> = UserQueryPro
            .selectBy().id.`is`.equalsTo(1)
            .or().name.`in`("Tom", "Cat", "Luo")
            .run()

        expectSqlResult("SELECT * FROM user WHERE user.id <> ?", listOf(2))
        UserQueryPro.selectBy().id.`is`.not.equalsTo(2).run()

        expectSqlResult("SELECT * FROM user WHERE UPPER(user.id) like UPPER(?)", listOf("%luo%"))
        UserQueryPro.selectBy().id.ignoreCase.like("%luo%").run()

        expectSqlResult("SELECT * FROM user WHERE user.id is null", listOf())
        UserQueryPro.selectBy().id.`is`.nul().run()

        expectSqlResult("SELECT * FROM user WHERE user.id = ? or (user.age = ? AND user.name like ?)", listOf(1, 20, "%Luo%"))
        UserQueryPro
            .selectBy().id.`is`.equalsTo(1)
            .or { it.age.equalsTo(20).and().name.like("%Luo%") }
            .run()

        expectSqlResult("SELECT * FROM user WHERE user.name like ? ORDER BY user.id DESC", listOf("%Luo%"))
        UserQueryPro.selectBy().name.like("%Luo%").orderBy().id().desc().run()

        expectSqlResult("SELECT * FROM user ORDER BY user.id DESC", listOf())
        UserQueryPro.orderBy().id().desc().run()

        expectSqlResult("SELECT * FROM user ORDER BY user.id DESC, user.name ASC", listOf())
        UserQueryPro.orderBy().id().desc().name().asc().run()

        expectSqlResult("SELECT * FROM user ORDER BY user.id DESC, user.name ASC LIMIT 1", listOf())
        UserQueryPro.orderBy().id().desc().name().asc().limit(1).run()

        expectSqlResult("SELECT user.id, user.name FROM user ORDER BY user.id DESC, user.name ASC LIMIT 1", listOf())
        UserQueryPro
            .orderBy().id().desc().name().asc().limit(1)
            .columnsLimiter().id().name()
            .run()

        expectSqlResult("SELECT user.id, user.age FROM user WHERE user.id = ?", listOf(1))
        UserQueryPro
            .selectBy().id.equalsTo(1).columnsLimiter().id().age().run()

        expectSqlResult("SELECT * FROM user LIMIT 1", listOf())
        val user: User? = UserQueryPro.selectBy().runLimit1()

        expectSqlResult(
            "SELECT setting.kee, setting.value FROM setting LEFT JOIN user ON setting.userId = user.id " +
                    "WHERE setting.kee = ? AND setting.value = ? AND UPPER(user.name) like UPPER(?) LIMIT 10",
            listOf("autoStart", true, "%luo%"))
        SettingQueryProEx // from setting
            .leftJoinOn(UserQueryProEx.joiner().id(), SettingQueryProEx.joiner().userId()) // left join user on user.id = setting.user_id
            .selectBy().kee.equalsTo("autoStart") // select ... where setting.kee = 'autoStart'
            .and().value.equalsTo(true) // and setting.value = true
            .andForeignField(UserQueryProEx.foreignField().name.ignoreCase.like("%luo%")) // and upper(user.name) like upper("%luo%")
            .limit(10) // limit 10
            .columnsLimiter().kee().value() // select setting.kee, setting.value from setting ...
            .run()

    }

    @Test
    fun testDelete() {
        UserQueryPro.deleteBy().id.equalsTo(1).run()
    }

    private fun expectSqlResult(sql: String, params: List<Any?>) {
        QueryStructureToSql.beforeReturnForTest = {
            assertEquals(it.first.trim(), sql.trim())
            assertContentEquals(it.second, params)
            QueryStructureToSql.beforeReturnForTest = null
        }
    }
}
