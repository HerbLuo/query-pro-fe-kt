package cn.cloudself.helpers

import com.alibaba.druid.pool.DruidDataSource
import javax.sql.DataSource
import cn.cloudself.query.structure_reolsver.QueryStructureToSql
import cn.cloudself.query.util.SqlUtils
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.intellij.lang.annotations.Language
import java.math.BigDecimal
import kotlin.test.assertEquals

var dbInitialized = false
const val DB_NAME = "query_pro_test"

private fun initDb() {
    if (dbInitialized) {
        return
    }

    val sqlGroup = """
        CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
        USE $DB_NAME;
        CREATE TABLE IF NOT EXISTS user (
            id   bigint auto_increment primary key,
            name varchar(55) null,
            age  int         null,
            deleted tinyint(1) null default false
        );
        CREATE TABLE IF NOT EXISTS setting (
            id      bigint auto_increment primary key,
            user_id bigint      null,
            kee     varchar(55) null,
            value   varchar(55) null,
            deleted tinyint(1) not null default false
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

@JvmOverloads
fun getDataSource(dbName: String? = DB_NAME, skipInit: Boolean = false): DataSource {
    if (!skipInit) {
        initDb()
    }

    val dataSource = DruidDataSource()
    dataSource.url = "jdbc:mysql://127.0.0.1:3306/${dbName ?: ""}?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
    dataSource.username = "root"
    dataSource.password = "123456"
    dataSource.driverClassName = "com.mysql.cj.jdbc.Driver"
    dataSource.queryTimeout = 16
    return dataSource
}

fun expectSqlResult(@Language("SQL") sql: String, params: List<Any?>) {
    QueryStructureToSql.beforeReturnForTest = {
        assertEquals(it.first.trim(), sql.trim())
        val second = it.second
        for (i in params.indices) {
            if (isNumber(params[i])) {
                assertEquals(params[i].toString(), second[i].toString())
            } else {
                assertEquals(params[i], second[i])
            }
        }
        QueryStructureToSql.beforeReturnForTest = null
    }
}

fun assertEqualsForJava(obj1: Any?, obj2: Any?) {
    if (obj1 is List<*> && obj2 is List<*>) {
        assertEquals(ArrayList(obj1), ArrayList(obj2))
    } else {
        assertEquals(obj1, obj2)
    }
}

private fun isNumber(obj: Any?): Boolean {
    return obj is Int || obj is Float || obj is Double || obj is Long || obj is BigDecimal || obj is Short
}

fun initLogger() {
    val builder = ConfigurationBuilderFactory.newConfigurationBuilder()

    val rootLogger = builder.newRootLogger(Level.ALL).add(builder.newAppenderRef("stdout"))
    builder.add(rootLogger)

    val layout = builder.newLayout("PatternLayout").addAttribute("pattern", "%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %highlight{%-5level}{FATAL=red blink, ERROR=red, WARN=yellow bold, INFO=green, DEBUG=gray, TRACE=blue} %style{%40.40C{1.}-%-4L}{cyan}: %msg%n%ex")
    val console = builder.newAppender("stdout", "Console")
    console.add(layout)
    builder.add(console)

    Configurator.initialize(builder.build())
}

