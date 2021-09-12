package cn.cloudself.helpers

import com.alibaba.druid.pool.DruidDataSource
import javax.sql.DataSource
import cn.cloudself.query.structure_reolsver.QueryStructureToSql
import org.intellij.lang.annotations.Language
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

var dbInitialized = false
const val DB_NAME = "query_pro_test"

fun initDb() {
    if (dbInitialized) {
        return
    }

    val sql = """
        CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
        CREATE TABLE user (
            id   bigint auto_increment primary key,
            name varchar(55) null,
            age  int         null
        );
        CREATE TABLE setting (
            id      bigint auto_increment primary key,
            user_id bigint      null,
            kee     varchar(55) null,
            value   varchar(55) null
        );
    """.trimIndent()

    val dataSource = getDataSource("", true)
    val connection = dataSource.connection
    val prepareStatement = connection.prepareStatement("SELECT 1")
    for (s in sql.split(";")) {
        prepareStatement.addBatch(s)
    }
    prepareStatement.executeBatch()
    dbInitialized = true
}

fun getDataSource(dbName: String = DB_NAME, skipInit: Boolean = false): DataSource {
    if (!skipInit) {
        initDb()
    }

    val dataSource = DruidDataSource()
    dataSource.url = "jdbc:mysql://127.0.0.1:3306/$dbName?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
    dataSource.username = "root"
    dataSource.password = "123456"
    dataSource.driverClassName = "com.mysql.cj.jdbc.Driver"
    return dataSource
}

fun expectSqlResult(@Language("SQL") sql: String, params: List<Any?>) {
    QueryStructureToSql.beforeReturnForTest = {
        assertEquals(it.first.trim(), sql.trim())
        assertContentEquals(it.second, params)
        QueryStructureToSql.beforeReturnForTest = null
    }
}

