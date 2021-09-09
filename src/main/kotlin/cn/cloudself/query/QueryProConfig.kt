package cn.cloudself.query

import cn.cloudself.query.structure_reolsver.JdbcQueryStructureResolver
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.Time
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.sql.DataSource
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "TYPE_MISMATCH_WARNING", "HasPlatformType")
fun <T: Enum<*>> enumValueOfAny(clazz: Class<*>, name: String) = java.lang.Enum.valueOf(clazz as Class<T>, name)

typealias ResultSetGetter<T> = (rs: ResultSet) -> (i: Int) -> T

typealias ResultSetParserEx = (rs: ResultSet, clazz: Class<*>, i: Int) -> Optional<Any>

data class DbColumnInfo(
    val type: String,
    val label: String,
)

object QueryProConfig {
    private var dataSource: DataSource? = null
    private val dataSourceThreadLocal: ThreadLocal<DataSource?> = ThreadLocal()
    private var supportedColumnType = mutableSetOf<Class<*>>()
    /**
     * ResultSet解析器
     * 内置支持的类型有:
     * BigDecimal, Byte, ByteArray, Date, LocalDate, LocalTime, LocalDateTime, java.sql.Date, Double, Float, Int,
     * Long, Time, Timestamp, Short, String,
     */
    private val resultSetParser = mutableMapOf<Class<*>, ResultSetGetter<*>>()
        .also { map ->
            @Suppress("UNCHECKED_CAST")
            fun <T: Any> put(clazz: KClass<T>, value: ResultSetGetter<T>) {
                val primitiveType = (clazz).javaPrimitiveType
                if (primitiveType != null && primitiveType != clazz.java) {
                    supportedColumnType.add(primitiveType)
                    map[primitiveType] = value
                }
                val objectType = clazz.javaObjectType
                if (objectType != clazz.java) {
                    supportedColumnType.add(objectType)
                    map[objectType] = value
                }

                supportedColumnType.add(clazz.java)
                map[clazz.java] = value
            }

            put(BigDecimal::class) { it::getBigDecimal }
            put(Byte::class) { it::getByte }
            put(ByteArray::class) { it::getBytes }
            put(Date::class) { it::getDate }
            put(LocalDate::class) { rs -> { i -> rs.getDate(i).toLocalDate() } }
            put(LocalTime::class) { rs -> { i -> rs.getTime(i).toLocalTime() } }
            put(LocalDateTime::class) { rs -> { i -> rs.getTimestamp(i).toLocalDateTime() } }
            put(java.sql.Date::class) { it::getDate }
            put(Double::class) { it::getDouble }
            put(Float::class) { it::getFloat }
            put(Int::class) { it::getInt }
            put(Long::class) { it::getLong }
            put(Time::class) { it::getTime }
            put(Timestamp::class) { it::getTimestamp }
            put(Short::class) { it::getShort }
            put(String::class) { it::getString }

            // 如果是未知的类型，使用meta data获取默认的java类型，如果一致，直接getObject
            // 否则抛出异常
        }
    val resultSetParserEx = mutableListOf<ResultSetParserEx>(
        { rs, clazz, i ->
            if (!clazz.isEnum) {
                Optional.empty()
            } else {
                @Suppress("UNCHECKED_CAST")
                Optional.ofNullable(enumValueOfAny(clazz, rs.getString(i)))
            }
        }
    )

    var beautifySql = true
    var printSql = true
    var dryRun: Boolean = false
    var queryProFieldComment = true
    var QueryStructureResolver: IQueryStructureResolver = JdbcQueryStructureResolver()
    val dbColumnInfoToJavaType = mutableMapOf<(column: DbColumnInfo) -> Boolean, Class<*>>(
        { column: DbColumnInfo -> (column.label == "id" || column.label.endsWith("_id")) && column.type.startsWith("BIGINT") } to Long::class.java,
    )

    @Suppress("UNCHECKED_CAST")
    fun <T> getResultSetParser(clazz: Class<T>): ResultSetGetter<T>? {
        var res = this.resultSetParser[clazz] as ResultSetGetter<T>?
        if (res == null) {
            for ((key, value) in this.resultSetParser) {
                if (key.isAssignableFrom(clazz)) {
                    res = value as ResultSetGetter<T>
                }
            }
        }
        return res
    }

    /**
     * 添加一个ResultSet解析器(字段解析器)
     * @param clazz 需要解析至的class, 例如: LocalDate.class
     * @param value 例子 rs -> i -> rs.getDate(i).toLocalDate()
     *
     * @see [resultSetParser]
     * @see [resultSetParserEx]
     */
    fun <T> addResultSetParser(clazz: Class<T>, value: ResultSetGetter<T>) = this.also {
        resultSetParser[clazz] = value
        supportedColumnType.add(clazz)
    }

    fun getSupportedColumnType() = supportedColumnType

    fun setDataSource(dataSource: DataSource) = this.also {
        this.dataSource = dataSource
    }

    fun setDataSourceThreadLocal(dataSource: DataSource) = this.also {
        dataSourceThreadLocal.set(dataSource)
    }

    fun getDataSourceOrInit(init: () -> DataSource): DataSource {
        val currentThreadDataSource = dataSourceThreadLocal.get()
        if (currentThreadDataSource != null) {
            return currentThreadDataSource
        }
        return dataSource ?: init().also { dataSource = it }
    }
}
