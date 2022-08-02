package cn.cloudself.query.config

import cn.cloudself.query.resolver.JdbcQSR
import java.math.BigDecimal
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

object QueryProConfig {
    @JvmField
    val global = GlobalQueryProConfigDb()
        .apply {
            setBeautifySql(true)
            setPrintSql(true)
            setPrintCallByInfo(true)
            setPrintResult(true)
            setDryRun(false)
            setQueryProFieldComment(true)
            setLogicDelete(true)
            setLogicDeleteField("deleted")
            setQueryStructureResolver(JdbcQSR())

            shouldIgnoreFields.add("serialVersionUID")

            // 数据库字段转Java字段 key: columnTester, value: JavaClass
            dbColumnInfoToJavaType[
                    // 将BIGINT类型的id列设置为Long类型
                    { column: DbColumnInfo -> (column.label == "id" || column.label.endsWith("_id")) && column.type.startsWith("BIGINT") }
            ] = Long::class.java

            /* jdbc查询的结果: resultSet转enum */
            resultSetParserEx.add { rs, clazz, i -> if (!clazz.isEnum) { Optional.empty() } else { Optional.ofNullable(enumValueOfAny(clazz, rs.getString(i))) } }

            // 这里是为了兼容性，所以将函数手动展开了
            putToResultSetParser(BigDecimal::class)    { rs, i -> rs.getBigDecimal(i) }
            putToResultSetParser(Byte::class)          { rs, i -> rs.getByte(i) }
            putToResultSetParser(ByteArray::class)     { rs, i -> rs.getBytes(i) }
            putToResultSetParser(Date::class)          { rs, i -> rs.getTimestamp(i) }
            putToResultSetParser(LocalDate::class)     { rs, i -> rs.getDate(i).toLocalDate() }
            putToResultSetParser(LocalTime::class)     { rs, i -> rs.getTime(i).toLocalTime() }
            putToResultSetParser(LocalDateTime::class) { rs, i -> rs.getTimestamp(i).toLocalDateTime() }
            putToResultSetParser(java.sql.Date::class) { rs, i -> rs.getDate(i) }
            putToResultSetParser(Double::class)        { rs, i -> rs.getDouble(i) }
            putToResultSetParser(Float::class)         { rs, i -> rs.getFloat(i) }
            putToResultSetParser(Int::class)           { rs, i -> rs.getInt(i) }
            putToResultSetParser(Long::class)          { rs, i -> rs.getLong(i) }
            putToResultSetParser(Time::class)          { rs, i -> rs.getTime(i) }
            putToResultSetParser(Timestamp::class)     { rs, i -> rs.getTimestamp(i) }
            putToResultSetParser(Short::class)         { rs, i -> rs.getShort(i) }
            putToResultSetParser(String::class)        { rs, i -> rs.getString(i) }
        }

    @JvmField
    val request = QueryProConfigDb(RequestContextStore())

    /**
     * 不推荐使用，优先使用global或request或者context
     *
     * 因为存在线程池复用线程, threadLocal不释放问题
     * 所以必须在线程初始化后调用QueryProConfig.thread.init()
     * 必须在线程结束后调用QueryProConfig.thread.clean()
     * 之后，才能针对对thread进行配置
     *
     * 另外：同时存在thread配置与request配置的时候，会使用request中的配置
     */
    @Deprecated("")
    @JvmField
    val thread: ThreadQueryProConfigDb = ThreadQueryProConfigDb()

    /**
     * 在回调函数中，维持一个query pro配置的上下文
     * 注意该配置对函数中新开的线程无效
     *
     * context不能嵌套
     *
     * QueryProConfig.context.use(context -> {
     *   context.beautifySql();
     *   UserQueryPro.selectBy().id().equalsTo(1);
     * });
     */
    @JvmField
    val context = ThreadQueryProConfigDb()

    /**
     * 内部使用
     */
    internal val code = CodeQueryProConfigDb()

    @Suppress("DEPRECATION")
    @JvmField
    val final = FinalQueryProConfigDb(arrayOf(code, context, request, thread, global))

    @Suppress("UNCHECKED_CAST", "TYPE_MISMATCH_WARNING", "HasPlatformType")
    private fun <T: Enum<*>> enumValueOfAny(clazz: Class<*>, name: String) = java.lang.Enum.valueOf(clazz as Class<T>, name)
}
