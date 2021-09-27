package cn.cloudself

import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.Setting
import cn.cloudself.helpers.query.User
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import cn.cloudself.query.exception.IllegalParameters
import org.junit.Test
import java.io.File
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class QueryProSqlTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE user;
                INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);
                INSERT INTO user (id, name, age) VALUES (2, 'hb', 10);
                TRUNCATE TABLE setting;
                INSERT INTO setting (id, user_id, kee, value) VALUES (1, 1, 'language', 'English');
            """
        ).update(Boolean::class.java)

        File("temp.sql").also {
            // language=SQL
            it.writeText( "SELECT * FROM setting")
            it.deleteOnExit()
        }
    }

    @Test
    fun test() {
        initLogger()

        QueryProConfig.beautifySql = false
        QueryProConfig.setDataSource(getDataSource())

        prepareData()

        val user1 = User(1, "hb", 18)
        val user2 = User(2, "hb", 10)

        val setting1 = Setting(1, 1, "language", "English")

        println("单条查询语句")
        QueryProSql.create("SELECT * FROM user").query(User::class.java)
            .also { users: List<User> -> assertContentEquals(users, listOf(user1, user2)) }

        println("单条查询语句 含分号")
        QueryProSql.create("SELECT * FROM user;").query(User::class.java)
            .also { users: List<User> -> assertContentEquals(users, listOf(user1, user2)) }

        println("单条查询语句 (文件模式)")
        QueryProSql.create(File("temp.sql").inputStream()).query(Setting::class.java)
            .also { settings: List<Setting> -> assertContentEquals(settings, listOf(setting1)) }

        println("单条更新语句")
        QueryProSql.create("INSERT INTO user (id, name, age) VALUES (3, 'herb', 18)").update()
            .also { affectRowCount: Int -> assertEquals(affectRowCount, 1) }

        println("单条更新语句, 含分号")
        QueryProSql.create("INSERT INTO user (id, name, age) VALUES (4, 'herb', 20);").update()
            .also { affectRowCount: Int -> assertEquals(affectRowCount, 1) }

        println("单条查询语句 查出单条数据")
        QueryProSql.create("SELECT * FROM user WHERE id = 3").queryOne(User::class.java)
            .also { user: User? -> assertEquals(user, User(3, "herb", 18)) }
        QueryProSql.create("SELECT * FROM user WHERE id = 4").queryOne(User::class.java)
            .also { user: User? -> assertEquals(user, User(4, "herb", 20)) }

        println("批量更新接口 单条语句多参数")
        val usersForBatchInsertByASqlAndMulParams = listOf(User(5, "mul-hb", 18), User(6, "mul-hb", 19), User(null, "mul-hb", 20))
        QueryProSql.createBatch(
            "INSERT INTO user (id, name, age) VALUES (?, ?, ?)",
            usersForBatchInsertByASqlAndMulParams.map { arrayOf<Any?>(it.id, it.name, it.age) }.toTypedArray()
        ).update()

        println("批量更新接口 多条语句多组参数(等数量)")
        QueryProSql.createBatch(
            arrayOf(
                "INSERT INTO user (id, name, age) VALUES (?, ?, ?)",
                "UPDATE user SET age = 20 WHERE id = 6;",
                "INSERT INTO setting (id, user_id, kee, value) VALUES (?, ?, ?, ?)",
            ),
            arrayOf(
                arrayOf(null, "mul-hb-same-num", 18),
                arrayOf(),
                arrayOf(null, 5, "language", "简体中文"),
            )
        ).update()

        QueryProSql.create("SELECT * FROM setting WHERE user_id = 5").queryOne(Setting::class.java)
            .also { setting: Setting? -> assertEquals(setting, Setting(2, 5, "language", "简体中文")) }

        println("批量更新接口 多条语句多组参数(不等数量, 报错)")
        assertFailsWith(IllegalParameters::class) {
            QueryProSql.createBatch(
                arrayOf(
                    "INSERT INTO user (id, name, age) VALUES (?, ?, ?); ",
                    "INSERT INTO (id, user_id, kee, value) VALUES (?, ?, ?, ?);"
                ),
                arrayOf(arrayOf(1, "hb", 18))
            ).update()
        }

        println("批量更新接口 语句参数对")
        QueryProSql.createBatch(arrayOf(
            "INSERT INTO user (id, name, age) VALUES (?, ?, ?)" to arrayOf(null, "mul-hb-pair", 18),
            "UPDATE user SET age = ? WHERE id = 5;" to arrayOf(17),
        )).update()

        println("批量更新语句更新成功")
        QueryProSql.create("SELECT * FROM user WHERE id = 5").queryOne(User::class.java)
            .also { user: User? -> assertEquals(user, User(5, "mul-hb", 17)) }

        println("user表总共有9条数据")
        QueryProSql.create("SELECT count(*) FROM user").queryOne(Int::class.java)
            .also { assertEquals(it, 9) }
    }
}