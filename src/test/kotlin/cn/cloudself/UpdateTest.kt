package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.query.User
import cn.cloudself.helpers.query.UserQueryPro
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import cn.cloudself.query.exception.MissingParameter
import org.junit.Test
import kotlin.test.assertFailsWith

class UpdateTest {
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

    @Test
    fun test() {
        QueryProConfig.dryRun = true
        QueryProConfig.beautifySql = false

        // 如果没有传入primary key, 且无where条件, 则报错
        assertFailsWith(MissingParameter::class) {
            UserQueryPro.updateSet(User(name = "hb")).run()
        }

        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(18, 1))
        UserQueryPro.updateSet(User(age = 18)).where.id.equalsTo(1).run()
            .also { assert(it) }

        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(18, 2))
        UserQueryPro.updateSet(User(id = 2, age = 18)).run()
            .also { assert(it) }

        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(18, 3))
        UserQueryPro.updateSet(User(id = 3, age = 18)).run()
            .also { assert(it) }

        expectSqlResult("UPDATE `user` SET `name` = ?, `age` = ? WHERE `user`.`id` = ?", listOf("hb", null, 2))
        UserQueryPro.updateSet(User(id = 2, name = "hb"), true).run()
            .also { assert(it) }

        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(18, 1))
        UserQueryPro.updateSet().id(1).age(18).run()
            .also { assert(it) }

        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`name` = ?", listOf(18, "herb"))
        UserQueryPro.updateSet().age(18).where.name.equalsTo("herb").run()
            .also { assert(it) }
    }
}
