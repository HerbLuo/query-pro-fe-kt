package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.User
import cn.cloudself.helpers.query.UserQueryPro
import cn.cloudself.query.NULL
import cn.cloudself.query.config.QueryProConfig
import cn.cloudself.query.QueryProSql
import cn.cloudself.query.exception.MissingParameter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE user;
                INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);
                INSERT INTO user (id, name, age) VALUES (2, 'hb', 10);
                INSERT INTO user (id, name, age) VALUES (3, 'herb', 18);
                INSERT INTO user (id, name, age) VALUES (4, 'l', 18);
                INSERT INTO user (id, name, age) VALUES (5, 'l', 18);
                INSERT INTO user (id, name, age) VALUES (6, 'lhb', 18);
                INSERT INTO user (id, name, age) VALUES (7, 'lhb', 20);
            """
        ).update(Boolean::class.java)
    }

    @Test
    fun test() {
        initLogger()

        QueryProConfig.global.setBeautifySql(false)
        QueryProConfig.global.setLogicDelete(false)
        QueryProConfig.global.setDataSource(getDataSource())

        prepareData()

        // 如果没有传入primary key, 且无where条件, 则报错
        assertFailsWith(MissingParameter::class) {
            UserQueryPro.updateSet(User(name = "hb")).run()
        }

        UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 18, false)), users) }
        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(19, 1))
        UserQueryPro.updateSet(User(age = 19)).where.id.equalsTo(1).run().also { assert(it) }
        UserQueryPro.selectBy().id.equalsTo(1).run().also { users: List<User> -> assertEquals(listOf(User(1, "hb", 19, false)), users) }

        UserQueryPro.selectBy().id.equalsTo(2).run().also { users: List<User> -> assertEquals(users, listOf(User(2, "hb", 10, false))) }
        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(18, 2))
        UserQueryPro.updateSet(User(id = 2, age = 18)).run().also { assert(it) }
        UserQueryPro.selectBy().id.equalsTo(2).run().also { users: List<User> -> assertEquals(users, listOf(User(2, "hb", 18, false))) }

        UserQueryPro.selectBy().id.equalsTo(3).run().also { users: List<User> -> assertEquals(users, listOf(User(3, "herb", 18, false))) }
        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(19, 3))
        UserQueryPro.updateSet(User(id = 3, age = 19)).run().also { assert(it) }
        UserQueryPro.selectBy().id.equalsTo(3).run().also { users: List<User> -> assertEquals(users, listOf(User(3, "herb", 19, false))) }

        UserQueryPro.selectBy().id.equalsTo(4).run().also { users: List<User> -> assertEquals(users, listOf(User(4, "l", 18, false))) }
        expectSqlResult("UPDATE `user` SET `name` = ?, `age` = ?, `deleted` = ? WHERE `user`.`id` = ?", listOf("hb", NULL, false, 4))
        UserQueryPro.updateSet(User(id = 4, name = "hb", deleted = false), true).run().also { assert(it) }
        UserQueryPro.selectBy().id.equalsTo(4).run().also { users: List<User> -> assertEquals(users, listOf(User(4, "hb", null, false))) }

        UserQueryPro.selectBy().id.equalsTo(5).run().also { users: List<User> -> assertEquals(users, listOf(User(5, "l", 18, false))) }
        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`id` = ?", listOf(NULL, 5))
        UserQueryPro.updateSet().id(5).age(NULL).run().also { assert(it) }
        UserQueryPro.selectBy().id.equalsTo(5).run().also { users: List<User> -> assertEquals(users, listOf(User(5, "l", null, false))) }

        UserQueryPro.selectBy().id.equalsTo(6).run().also { users: List<User> -> assertEquals(users, listOf(User(6, "lhb", 18, false))) }
        UserQueryPro.selectBy().id.equalsTo(7).run().also { users: List<User> -> assertEquals(users, listOf(User(7, "lhb", 20, false))) }
        expectSqlResult("UPDATE `user` SET `age` = ? WHERE `user`.`name` = ?", listOf(22, "lhb"))
        UserQueryPro.updateSet().age(22).where.name.equalsTo("lhb").run().also { assert(it) }
        UserQueryPro.selectBy().id.equalsTo(6).run().also { users: List<User> -> assertEquals(users, listOf(User(6, "lhb", 22, false))) }
        UserQueryPro.selectBy().id.equalsTo(7).run().also { users: List<User> -> assertEquals(users, listOf(User(7, "lhb", 22, false))) }
    }
}
