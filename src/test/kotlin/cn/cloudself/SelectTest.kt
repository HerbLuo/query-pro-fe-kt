package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.query.*
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import org.intellij.lang.annotations.Language
import org.junit.Test
import kotlin.test.assertContentEquals

class SelectTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE user;
                INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);
                INSERT INTO user (id, name, age) VALUES (2, 'hb', 10);
                INSERT INTO user (id, name, age) VALUES (3, 'herb', 18);
                INSERT INTO user (id, name, age) VALUES (4, 'l', 18);
                TRUNCATE TABLE setting;
                INSERT INTO setting (id, user_id, kee, value) VALUES (1, 1, 'lang', '简体中文');
            """
        ).update(Boolean::class.java)
    }

    @Test
    fun test() {
        QueryProConfig.beautifySql = false
        QueryProConfig.setDataSource(getDataSource())

        prepareData()

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ?", listOf(1))
        val users: List<User> = UserQueryPro.selectBy().id.equalsTo(1).run()
        assertContentEquals(users, listOf(User(1, "hb", 18)))

        expectSqlResult("SELECT `setting`.`id` FROM `setting` WHERE `setting`.`id` = ?", listOf(1))
        val ids: List<Long?> = SettingQueryPro.selectBy().id.equalsTo(1).columnLimiter().id()
        assertContentEquals(ids, listOf(1))

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` = ? AND `user`.`age` = ?", listOf("hb", 18))
        val users1: List<User> = UserQueryPro.selectBy().name.`is`.equalsTo("hb").and().age.`is`.equalsTo(18).run()

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`age` = ? or `user`.`name` in (?, ?)", listOf(18, "hb", "herb"))
        val users2: List<User> = UserQueryPro
            .selectBy().age.`is`.equalsTo(18)
            .or().name.`in`("hb", "herb")
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
}