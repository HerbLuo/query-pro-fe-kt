package cn.cloudself

import cn.cloudself.helpers.expectSqlResult
import cn.cloudself.helpers.getDataSource
import cn.cloudself.helpers.initLogger
import cn.cloudself.helpers.query.SettingQueryPro
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryProSql
import org.junit.Test
import kotlin.test.assertEquals

class LogicDeleteTest {
    private fun prepareData() {
        QueryProSql.createBatchBySqlGroup(
            """
                TRUNCATE TABLE setting;
                INSERT INTO setting (id, user_id, kee, value, deleted) VALUES (1, 1, 'lang', 'en', false);
                INSERT INTO setting (id, user_id, kee, value, deleted) VALUES (2, 2, 'lang', 'english', false);
                INSERT INTO setting (id, user_id, kee, value, deleted) VALUES (3, 3, 'lang', 'zh-cn', false);
                INSERT INTO setting (id, user_id, kee, value, deleted) VALUES (4, 4, 'lang', '简体中文', false);
            """
        ).update(Boolean::class.java)
    }

    @Test
    fun test() {
        initLogger()

        QueryProConfig.beautifySql = false
        QueryProConfig.logicDelete = true
        QueryProConfig.setDataSource(getDataSource())

        prepareData()

        expectSqlResult("UPDATE `setting` SET `deleted` = ? WHERE `setting`.`id` = ?", listOf(true, 1))
        SettingQueryPro.deleteBy().id.equalsTo(1).run().also { assert(it) }
        SettingQueryPro.selectBy().id.equalsTo(1).runLimit1().also { assertEquals(it, null) }
    }
}