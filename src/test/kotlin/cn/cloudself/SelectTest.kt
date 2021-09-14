package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.query.*
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SelectTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE user;
                INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);
                INSERT INTO user (id, name, age) VALUES (2, 'hb', 10);
                INSERT INTO user (id, name, age) VALUES (3, 'herb', 18);
                INSERT INTO user (id, name, age) VALUES (4, 'l', null);
                TRUNCATE TABLE setting;
                INSERT INTO setting (id, user_id, kee, value) VALUES (1, 1, 'lang', '简体中文');
                INSERT INTO setting (id, user_id, kee, value) VALUES (2, 1, 'theme', 'dark');
                INSERT INTO setting (id, user_id, kee, value) VALUES (3, 2, 'lang', '繁体中文');
            """
        ).update(Boolean::class.java)
    }

    @Suppress("SqlRedundantOrderingDirection")
    @Test
    fun test() {
        QueryProConfig.beautifySql = false
        QueryProConfig.setDataSource(getDataSource())

        prepareData()

        val user1 = User(1, "hb", 18)
        val user2 = User(2, "hb", 10)
        val user3 = User(3, "herb", 18)
        val user4 = User(4, "l", null)

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ?", listOf(1))
        UserQueryPro.selectBy().id.equalsTo(1).run()
            .also { users: List<User> -> assertEquals(users, listOf(user1)) }

        expectSqlResult("SELECT `setting`.`id` FROM `setting` WHERE `setting`.`id` = ?", listOf(1))
        SettingQueryPro.selectBy().id.equalsTo(1).columnLimiter().id()
            .also { ids: List<Long?> -> assertContentEquals(ids, listOf(1)) }

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` = ? AND `user`.`age` = ?", listOf("hb", 18))
        UserQueryPro.selectBy().name.`is`.equalsTo("hb").and().age.`is`.equalsTo(18).run()
            .also { users: List<User> -> assertEquals(users, listOf(user1)) }

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` in (?, ?)", listOf("hb", "herb"))
        UserQueryPro.selectBy().name.`in`("hb", "herb").run()
            .also { users: List<User> -> assertEquals(users, listOf(user1, user2, user3)) }

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ? or `user`.`age` = ?", listOf(1, 10))
        UserQueryPro
            .selectBy().id.`is`.equalsTo(1)
            .or().age.equalsTo(10)
            .run()
            .also { users: List<User> -> assertEquals(users, listOf(user1, user2)) }

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` <> ?", listOf(2))
        UserQueryPro.selectBy().id.`is`.not.equalsTo(2).run()
            .also { users: List<User> -> assertEquals(users, listOf(user1, user3, user4)) }

        expectSqlResult("SELECT * FROM `user` WHERE UPPER(`user`.`name`) like UPPER(?)", listOf("%H%"))
        UserQueryPro.selectBy().name.ignoreCase.like("%H%").run()
            .also { users: List<User> -> assertEquals(users, listOf(user1, user2, user3)) }

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`age` is null", listOf())
        UserQueryPro.selectBy().age.`is`.nul().run()
            .also { users: List<User> -> assertEquals(users, listOf(user4)) }

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ? or (`user`.`age` = ? AND `user`.`name` like ?)", listOf(1, 18, "%rb%"))
        UserQueryPro
            .selectBy().id.`is`.equalsTo(1)
            .or { it.age.equalsTo(18).and().name.like("%rb%") }
            .run()
            .also { users: List<User> -> assertEquals(users, listOf(user1, user3)) }

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` like ? ORDER BY `user`.`id` DESC", listOf("%h%"))
        UserQueryPro.selectBy().name.like("%h%").orderBy().id().desc().run()
            .also { users: List<User> -> assertEquals(users, listOf(user3, user2, user1)) }

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`id` DESC", listOf())
        UserQueryPro.orderBy().id().desc().run()
            .also { users: List<User> -> assertEquals(users, listOf(user4, user3, user2, user1)) }

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`age` ASC, `user`.`id` DESC", listOf())
        UserQueryPro.orderBy().age().asc().id().desc().run()
            .also { users: List<User> -> assertEquals(users, listOf(user4, user2, user3, user1)) }

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`age` DESC, `user`.`id` ASC LIMIT 1", listOf())
        UserQueryPro.orderBy().age().desc().id().asc().limit(1).run()
            .also { users: List<User> -> assertEquals(users, listOf(user1)) }

        expectSqlResult("SELECT * FROM `user` LIMIT 1", listOf())
        UserQueryPro.selectBy().runLimit1()
            .also { user: User? -> assertEquals(user, user1) }

        expectSqlResult("SELECT `user`.`id`, `user`.`age` FROM `user` WHERE `user`.`id` = ?", listOf(1))
        UserQueryPro.selectBy().id.equalsTo(1).columnsLimiter().id().age().run()
            .also { users: List<User> -> assertEquals(users, listOf(user1.copy(name = null))) }

        expectSqlResult("SELECT `user`.`id`, `user`.`name` FROM `user` ORDER BY `user`.`age` DESC, `user`.`id` DESC LIMIT 1", listOf())
        UserQueryPro
            .orderBy().age().desc().id().desc().limit(1)
            .columnsLimiter().id().name()
            .run()
            .also { users: List<User> -> assertEquals(users, listOf(user3.copy(age = null))) }

        expectSqlResult(
            "SELECT `setting`.`kee`, `setting`.`value` FROM `setting` LEFT JOIN `user` ON `setting`.`user_id` = `user`.`id` " +
                    "WHERE `setting`.`kee` = ? AND `setting`.`value` like ? AND UPPER(`user`.`name`) like UPPER(?) LIMIT 10",
            listOf("lang", "%中文", "%H%"))
        SettingQueryProEx // from setting
            .leftJoinOn(UserQueryProEx.joiner().id(), SettingQueryProEx.joiner().userId()) // left join user on user.id = setting.user_id
            .selectBy().kee.equalsTo("lang") // select ... where setting.kee = 'lang'
            .and().value.`is`.like("%中文") // and setting.value like '%中文'
            .andForeignField(UserQueryProEx.foreignField().name.ignoreCase.like("%H%")) // and upper(user.name) like upper("%luo%")
            .limit(10) // limit 10
            .columnsLimiter().kee().value() // select setting.kee, setting.value from setting ...
            .run()
            .also { settings: List<Setting> -> assertEquals(settings, listOf(Setting(kee = "lang", value = "简体中文"), Setting(kee = "lang", value = "繁体中文"))) }
    }
}