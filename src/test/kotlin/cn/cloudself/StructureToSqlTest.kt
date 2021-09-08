package cn.cloudself

import cn.cloudself.query.*
import cn.cloudself.query.exception.IllegalCall
import cn.cloudself.query.exception.MissingParameter
import cn.cloudself.query.structure_reolsver.QueryStructureToSql
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Suppress("UNUSED_VARIABLE")
class StructureToSqlTest {
    @Test
    fun testSelect() {
        QueryProConfig.dryRun = true
        QueryProConfig.beautifySql = false

        expectSqlResult("SELECT `setting`.`id` FROM `setting` WHERE `setting`.`id` = ?", listOf(1))
        val ids: List<Long?> = SettingQueryPro.selectBy().id.equalsTo(1).columnLimiter().id()

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` = ? AND `user`.`age` = ?", listOf(1, 1000))
        val users1: List<User> = UserQueryPro.selectBy().name.`is`.equalsTo(1).and().age.`is`.equalsTo(1000).run()

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ? or `user`.`name` in (?, ?, ?)", listOf(1, "Tom", "Cat", "Luo"))
        val users2: List<User> = UserQueryPro
            .selectBy().id.`is`.equalsTo(1)
            .or().name.`in`("Tom", "Cat", "Luo")
            .run()

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` <> ?", listOf(2))
        UserQueryPro.selectBy().id.`is`.not.equalsTo(2).run()

        expectSqlResult("SELECT * FROM `user` WHERE UPPER(`user`.`id`) like UPPER(?)", listOf("%luo%"))
        UserQueryPro.selectBy().id.ignoreCase.like("%luo%").run()

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` is null", listOf())
        UserQueryPro.selectBy().id.`is`.nul().run()

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ? or (`user`.`age` = ? AND `user`.`name` like ?)", listOf(1, 20, "%Luo%"))
        UserQueryPro
            .selectBy().id.`is`.equalsTo(1)
            .or { it.age.equalsTo(20).and().name.like("%Luo%") }
            .run()

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` like ? ORDER BY `user`.`id` DESC", listOf("%Luo%"))
        UserQueryPro.selectBy().name.like("%Luo%").orderBy().id().desc().run()

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`id` DESC", listOf())
        UserQueryPro.orderBy().id().desc().run()

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`id` DESC, `user`.`name` ASC", listOf())
        UserQueryPro.orderBy().id().desc().name().asc().run()

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`id` DESC, `user`.`name` ASC LIMIT 1", listOf())
        UserQueryPro.orderBy().id().desc().name().asc().limit(1).run()

        expectSqlResult("SELECT `user`.`id`, `user`.`name` FROM `user` ORDER BY `user`.`id` DESC, `user`.`name` ASC LIMIT 1", listOf())
        UserQueryPro
            .orderBy().id().desc().name().asc().limit(1)
            .columnsLimiter().id().name()
            .run()

        expectSqlResult("SELECT `user`.`id`, `user`.`age` FROM `user` WHERE `user`.`id` = ?", listOf(1))
        UserQueryPro
            .selectBy().id.equalsTo(1).columnsLimiter().id().age().run()

        expectSqlResult("SELECT * FROM `user` LIMIT 1", listOf())
        val user: User? = UserQueryPro.selectBy().runLimit1()

        expectSqlResult(
            "SELECT `setting`.`kee`, `setting`.`value` FROM `setting` LEFT JOIN `user` ON `setting`.`user_id` = `user`.`id` " +
                    "WHERE `setting`.`kee` = ? AND `setting`.`value` = ? AND UPPER(`user`.`name`) like UPPER(?) LIMIT 10",
            listOf("autoStart", true, "%luo%"))
        SettingQueryProEx // from setting
            .leftJoinOn(UserQueryProEx.joiner().id(), SettingQueryProEx.joiner().userId()) // left join user on user.id = setting.user_id
            .selectBy().kee.equalsTo("autoStart") // select ... where setting.kee = 'autoStart'
            .and().value.`is`.equalsTo(true) // and setting.value = true
            .andForeignField(UserQueryProEx.foreignField().name.ignoreCase.like("%luo%")) // and upper(user.name) like upper("%luo%")
            .limit(10) // limit 10
            .columnsLimiter().kee().value() // select setting.kee, setting.value from setting ...
            .run()

    }

    @Test
    fun testDelete() {
        QueryProConfig.dryRun = true
        QueryProConfig.beautifySql = false

        expectSqlResult("DELETE FROM `user` WHERE `user`.`id` = ?", listOf(1))
        val success: Boolean = UserQueryPro.deleteBy().id.equalsTo(1).run()
    }

    @Test
    fun testUpdate() {
        QueryProConfig.dryRun = true
        QueryProConfig.beautifySql = false

        // 如果没有传入primary key, 且无where条件, 则报错
        assertFailsWith(MissingParameter::class) {
            UserQueryPro.updateSet(User(name = "hb")).run()
        }

        expectSqlResult("UPDATE `user` SET `age` = 18 WHERE `user`.`id` = ?", listOf(1))
        val success1: Boolean = UserQueryPro.updateSet(User(age = 18)).where.id.equalsTo(1).run()

        expectSqlResult("UPDATE `user` SET `age` = 18 WHERE `user`.`id` = ?", listOf(2L))
        val success2: Boolean = UserQueryPro.updateSet(User(id = 2, age = 18)).run()

        expectSqlResult("UPDATE `user` SET `age` = 18 WHERE `user`.`id` = ?", listOf(3L))
        val success3: Boolean = UserQueryPro.updateSet(User(id = 3, age = 18)).run()

        expectSqlResult("UPDATE `user` SET `name` = hb, `age` = null WHERE `user`.`id` = ?", listOf(2L))
        val success5: Boolean = UserQueryPro.updateSet(User(id = 2, name = "hb"), true).run()

        expectSqlResult("UPDATE `user` SET `age` = 18 WHERE `user`.`id` = ?", listOf(1))
        val success6: Boolean = UserQueryPro.updateSet().id(1).age(18).run()

        expectSqlResult("UPDATE `user` SET `age` = 18 WHERE `user`.`name` = ?", listOf("herb"))
        val success7: Boolean = UserQueryPro.updateSet().age(18).where.name.equalsTo("herb").run()
    }

    private fun expectSqlResult(sql: String, params: List<Any?>) {
        QueryStructureToSql.beforeReturnForTest = {
            assertEquals(it.first.trim(), sql.trim())
            assertContentEquals(it.second, params)
            QueryStructureToSql.beforeReturnForTest = null
        }
    }
}
