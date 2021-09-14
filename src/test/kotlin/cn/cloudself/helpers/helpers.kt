package cn.cloudself.helpers

import com.alibaba.druid.pool.DruidDataSource
import javax.sql.DataSource
import cn.cloudself.query.structure_reolsver.QueryStructureToSql
import cn.cloudself.query.util.SqlUtils
import org.intellij.lang.annotations.Language
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

var dbInitialized = false
const val DB_NAME = "query_pro_test"

fun initDb() {
    if (dbInitialized) {
        return
    }

    val sqlGroup = """
        CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
        USE $DB_NAME;
        CREATE TABLE IF NOT EXISTS user (
            id   bigint auto_increment primary key,
            name varchar(55) null,
            age  int         null
        );
        CREATE TABLE IF NOT EXISTS setting (
            id      bigint auto_increment primary key,
            user_id bigint      null,
            kee     varchar(55) null,
            value   varchar(55) null
        );
    """.trimIndent()

    val dataSource = getDataSource(null, true)
    val connection = dataSource.connection
    val statement = connection.createStatement()
    val splitBySemicolonAndCountQuestionMark = SqlUtils.splitBySemicolonAndCountQuestionMark(sqlGroup)
    for ((sql) in splitBySemicolonAndCountQuestionMark) {
        statement.addBatch(sql)
    }
    statement.executeBatch()
    dbInitialized = true
}

fun getDataSource(dbName: String? = DB_NAME, skipInit: Boolean = false): DataSource {
    if (!skipInit) {
        initDb()
    }

    val dataSource = DruidDataSource()
    dataSource.url = "jdbc:mysql://127.0.0.1:3306/${dbName ?: ""}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
    dataSource.username = "root"
    dataSource.password = "123456"
    dataSource.driverClassName = "com.mysql.cj.jdbc.Driver"
    dataSource.queryTimeout = 16
    return dataSource
}

fun expectSqlResult(@Language("SQL") sql: String, params: List<Any?>) {
    QueryStructureToSql.beforeReturnForTest = {
        assertEquals(it.first.trim(), sql.trim())
        assertContentEquals(it.second, params)
        QueryStructureToSql.beforeReturnForTest = null
    }
}

