package cn.cloudself.query.config

import cn.cloudself.query.resolver.IQueryStructureResolver
import cn.cloudself.query.QueryPayload
import cn.cloudself.query.QueryStructure
import cn.cloudself.query.exception.IllegalCall
import cn.cloudself.query.exception.IllegalImplements
import cn.cloudself.query.util.Result
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import javax.sql.DataSource
import kotlin.reflect.KClass

typealias QueryStructureTransformer = (Class<*>, QueryStructure, QueryPayload) -> Result<QueryStructure, Throwable>

typealias ResultTransformer = (result: Any, QueryStructure, QueryPayload) -> Result<Any, Throwable>

fun interface ResultSetGetter<T> {
    @Throws(SQLException::class)
    fun get(rs: ResultSet, i: Int): T?
}

fun interface ResultSetParserEx {
    @Throws(SQLException::class)
    fun parse(rs: ResultSet, clazz: Class<*>, i: Int): Optional<Any>
}

data class DbColumnInfo(
    val type: String,
    val label: String,
)

interface IQueryProConfigDb<DataSource, Boolean, String, IQueryStructureResolver> {
    fun dataSource(): DataSource
    fun beautifySql(): Boolean
    fun printSql(): Boolean
    fun printCallByInfo(): Boolean
    fun printResult(): Boolean
    fun dryRun(): Boolean
    fun queryProFieldComment(): Boolean
    fun logicDelete(): Boolean
    fun logicDeleteField(): String
    fun queryStructureResolver(): IQueryStructureResolver
}

interface IQueryProConfigDbWriteable {
    fun setDataSource(dataSource: DataSource): IQueryProConfigDbWriteable
    fun setBeautifySql(beautifySql: Boolean): IQueryProConfigDbWriteable
    fun setPrintSql(printSql: Boolean): IQueryProConfigDbWriteable
    fun setPrintCallByInfo(printCallByInfo: Boolean): IQueryProConfigDbWriteable
    fun setPrintResult(printResult: Boolean): IQueryProConfigDbWriteable
    fun setDryRun(dryRun: Boolean): IQueryProConfigDbWriteable
    fun setQueryProFieldComment(queryProFieldComment: Boolean): IQueryProConfigDbWriteable
    fun setLogicDelete(logicDelete: Boolean): IQueryProConfigDbWriteable
    fun setLogicDeleteField(logicDeleteField: String): IQueryProConfigDbWriteable
    fun setQueryStructureResolver(queryStructureResolver: IQueryStructureResolver): IQueryProConfigDbWriteable
}

typealias NullableQueryProConfigDb = IQueryProConfigDb<DataSource?, Boolean?, String?, IQueryStructureResolver?>
typealias NonNullQueryProConfigDb = IQueryProConfigDb<DataSource, Boolean, String, IQueryStructureResolver>

interface Store {
    fun get(key: String): Any?
    fun set(key: String, value: Any?)
}

open class QueryProConfigDb(private val store: Store): NullableQueryProConfigDb, IQueryProConfigDbWriteable {
    override fun dataSource()             = store.get("dataSource") as DataSource?
    override fun beautifySql()            = store.get("beautifySql") as Boolean?
    override fun printSql()               = store.get("printSql") as Boolean?
    override fun printCallByInfo()        = store.get("printCallByInfo") as Boolean?
    override fun printResult()            = store.get("printResult") as Boolean?
    override fun dryRun()                 = store.get("dryRun") as Boolean?
    override fun queryProFieldComment()   = store.get("queryProFieldComment") as Boolean?
    override fun logicDelete()            = store.get("logicDelete") as Boolean?
    override fun logicDeleteField()       = store.get("logicDeleteField") as String?
    override fun queryStructureResolver() = store.get("queryStructureResolver") as IQueryStructureResolver?
    override fun setDataSource(dataSource: DataSource)                                      = this.also { this.store.set("dataSource", dataSource) }
    override fun setBeautifySql(beautifySql: Boolean)                                       = this.also { this.store.set("beautifySql", beautifySql) }
    override fun setPrintSql(printSql: Boolean)                                             = this.also { this.store.set("printSql", printSql) }
    override fun setPrintCallByInfo(printCallByInfo: Boolean)                               = this.also { this.store.set("printCallByInfo", printCallByInfo) }
    override fun setPrintResult(printResult: Boolean)                                       = this.also { this.store.set("printResult", printResult) }
    override fun setDryRun(dryRun: Boolean)                                                 = this.also { this.store.set("dryRun", dryRun) }
    override fun setQueryProFieldComment(queryProFieldComment: Boolean)                     = this.also { this.store.set("queryProFieldComment", queryProFieldComment) }
    override fun setLogicDelete(logicDelete: Boolean)                                       = this.also { this.store.set("logicDelete", logicDelete) }
    override fun setLogicDeleteField(logicDeleteField: String)                              = this.also { this.store.set("logicDeleteField", logicDeleteField) }
    override fun setQueryStructureResolver(queryStructureResolver: IQueryStructureResolver) = this.also { this.store.set("queryStructureResolver", queryStructureResolver) }
}

typealias ConfigStore = Map<String, Any?>

open class HashMapStore: Store {
    private val store = mutableMapOf<String, Any?>()
    override fun get(key: String): Any? = store[key]
    override fun set(key: String, value: Any?) { store[key] = value }
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        for (key in store.keys) {
            map[key] = this.get(key)
        }
        return map
    }
}

class CodeStore(private val clazz: Class<*>? = null): HashMapStore() {
    init {
        super.set("dataSource", null)
    }

    override fun get(key: String): Any? {
        val value = super.get(key)
        if (key == "dataSource") {
            if (value == null) {
                return QueryProConfig.global.defaultDataSource().get(clazz)
            }
        }
        return value
    }
}

private const val KEY_PREFIX = "QUERY_PRO_CONFIG:REQUEST_CONTEXT:"
class RequestContextStore: Store {
    private val isRequestContextHolderPresent = try {
        Class.forName("org.springframework.web.context.request.RequestContextHolder")
        true
    } catch (e: Throwable) {
        false
    }

    override fun get(key: String): Any? {
        if (!isRequestContextHolderPresent) {
            return null
        }

        return try {
            RequestContextHolder.currentRequestAttributes().getAttribute("$KEY_PREFIX$key", RequestAttributes.SCOPE_REQUEST)
        } catch (e: Exception) {
            null
        }
    }

    override fun set(key: String, value: Any?) {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        RequestContextHolder.currentRequestAttributes().setAttribute("$KEY_PREFIX$key", value, RequestAttributes.SCOPE_REQUEST)
    }
}

class ThreadContextStore constructor(private val init: MutableMap<String, Any?>? = null): Store {
    private val store: ThreadLocal<MutableMap<String, Any?>?> = ThreadLocal.withInitial { init }
    private var initCalled = false

    fun init() {
        store.remove()
        initCalled = true
    }

    fun clean() {
        init()
    }

    override fun get(key: String): Any? {
        return store.get()?.get(key)
    }

    override fun set(key: String, value: Any?) {
        if (!initCalled) {
            throw IllegalCall("使用thread进行配置时，因为线程池的问题，" +
                    "必须在线程初始化后调用initThreadContextStore，线程结束时调用cleanThreadContextStore，" +
                    "所以spring环境更推荐使用request进行配置。")
        }
        var map = store.get()
        if (map == null) {
            map = mutableMapOf()
            store.set(map)
        }
        map[key] = value
    }

    fun getStore(): Map<String, Any?>? {
        return store.get()
    }

    fun setStore(map: MutableMap<String, Any?>) {
        store.set(map)
    }
}

fun interface DataSourceGetter {
    fun get(clazz: Class<*>?): DataSource?
}

interface OnlyGlobalConfig {
    fun lifecycle(): Lifecycle
    fun defaultDataSource(): DataSourceGetter
    fun setDefaultDataSource(getter: DataSourceGetter): GlobalQueryProConfigDb
    fun shouldIgnoreFields(): Set<String>
    fun supportedColumnType(): Set<Class<*>>
    fun resultSetParserEx(): List<ResultSetParserEx>
    fun dbColumnInfoToJavaType(): Map<(column: DbColumnInfo) -> Boolean, Class<*>>
    fun <T> resultSetParser(clazz: Class<T>): ResultSetGetter<T>?
}

class GlobalQueryProConfigDb: QueryProConfigDb(HashMapStore()), OnlyGlobalConfig {
    private val lifecycle = Lifecycle()
    private val supportedColumnType = mutableSetOf<Class<*>>()
    private val resultSetParser = mutableMapOf<Class<*>, ResultSetGetter<*>>()
    private var defaultDataSource = DataSourceGetter { null }
    internal val resultSetParserEx = mutableListOf<ResultSetParserEx>()
    internal val dbColumnInfoToJavaType = mutableMapOf<(column: DbColumnInfo) -> Boolean, Class<*>>()
    internal val shouldIgnoreFields = mutableSetOf<String>()

    override fun lifecycle(): Lifecycle = lifecycle

    override fun shouldIgnoreFields(): Set<String> = shouldIgnoreFields
    override fun supportedColumnType(): Set<Class<*>> = supportedColumnType
    override fun resultSetParserEx(): List<ResultSetParserEx> = resultSetParserEx
    override fun dbColumnInfoToJavaType(): Map<(column: DbColumnInfo) -> Boolean, Class<*>> = dbColumnInfoToJavaType

    override fun defaultDataSource() = defaultDataSource
    override fun setDefaultDataSource(getter: DataSourceGetter) = this.also { defaultDataSource = getter }

    /**
     * ResultSet解析器
     * 内置支持的类型有:
     * BigDecimal, Byte, ByteArray, Date, LocalDate, LocalTime, LocalDateTime, java.sql.Date, Double, Float, Int,
     * Long, Time, Timestamp, Short, String,
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> resultSetParser(clazz: Class<T>): ResultSetGetter<T>? {
        var res = this.resultSetParser[clazz] as ResultSetGetter<T>?
        if (res == null) {
            for ((key, value) in this.resultSetParser) {
                if (key.isAssignableFrom(clazz)) {
                    res = value as ResultSetGetter<T>
                    break
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

    /**
     * 添加一个ResultSet解析器，与addResultSetParser功能相似，但更推荐使用addResultSetParser，因为性能略好
     */
    fun addResultSetParserEx(parser: ResultSetParserEx) = this.also {
        resultSetParserEx.add(parser)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T: Any> putToResultSetParser(clazz: KClass<T>, value: ResultSetGetter<T>) {
        val primitiveType = (clazz).javaPrimitiveType
        if (primitiveType != null && primitiveType != clazz.java) {
            supportedColumnType.add(primitiveType)
            resultSetParser[primitiveType] = value
        }
        val objectType = clazz.javaObjectType
        if (objectType != clazz.java) {
            supportedColumnType.add(objectType)
            resultSetParser[objectType] = value
        }

        supportedColumnType.add(clazz.java)
        resultSetParser[clazz.java] = value
    }
}

class FinalQueryProConfigDb(private val configs: Array<NullableQueryProConfigDb>): NonNullQueryProConfigDb, OnlyGlobalConfig {
    private fun <R> getBy(getter: (db: NullableQueryProConfigDb) -> R?): R = getByNullable(getter)
        ?: throw IllegalImplements("遍历了所有配置但仍然无法找到有效配置")
    private fun <R> getByNullable(getter: (db: NullableQueryProConfigDb) -> R?): R? {
        for (config in configs) {
            val res = getter(config)
            if (res != null) {
                return res
            }
        }
        return null
    }

    fun dataSourceNullable() = getByNullable { it.dataSource() }
    override fun dataSource() = getBy { it.dataSource() }
    override fun beautifySql() = getBy { it.beautifySql() }
    override fun printSql() = getBy { it.printSql() }
    override fun printCallByInfo() = getBy { it.printCallByInfo() }
    override fun printResult() = getBy { it.printResult() }
    override fun dryRun() = getBy { it.dryRun() }
    override fun queryProFieldComment() = getBy { it.queryProFieldComment() }
    override fun logicDelete() = getBy { it.logicDelete() }
    override fun logicDeleteField() = getBy { it.logicDeleteField() }
    override fun queryStructureResolver() = getBy { it.queryStructureResolver() }

    override fun lifecycle(): Lifecycle = QueryProConfig.global.lifecycle()
    override fun defaultDataSource() = QueryProConfig.global.defaultDataSource()
    override fun setDefaultDataSource(getter: DataSourceGetter) = QueryProConfig.global.setDefaultDataSource(getter)
    override fun shouldIgnoreFields(): Set<String> = QueryProConfig.global.shouldIgnoreFields()
    override fun supportedColumnType() = QueryProConfig.global.supportedColumnType()
    override fun resultSetParserEx() = QueryProConfig.global.resultSetParserEx()
    override fun dbColumnInfoToJavaType() = QueryProConfig.global.dbColumnInfoToJavaType()
    override fun <T> resultSetParser(clazz: Class<T>) = QueryProConfig.global.resultSetParser(clazz)
}

open class ThreadQueryProConfigDb(
    internal val store: ThreadContextStore = ThreadContextStore(),
    internal val configDb: QueryProConfigDb = QueryProConfigDb(store),
): NullableQueryProConfigDb by configDb, IQueryProConfigDbWriteable by configDb {
    fun init() {
        store.init()
    }

    fun clean() {
        store.clean()
    }

    fun use(func: Use) {
        use {
            func.call(it)
        }
    }

    fun <T> use(func: UseResult<T>): T {
        return use {
            func.call(it)
        }
    }

    @JvmName("_useKt")
    fun <T> use(func: (context: QueryProConfigDb) -> T): T {
        store.init()
        val result = try {
            func(configDb)
        } finally {
            store.clean()
        }
        return result!!
    }

    interface Use {
        @Throws(Exception::class)
        fun call(context: QueryProConfigDb)
    }

    interface UseResult<T> {
        @Throws(Exception::class)
        fun call(context: QueryProConfigDb): T
    }
}

class CodeQueryProConfigDb: ThreadQueryProConfigDb() {
    fun <T> use(store: Map<String, Any?>, func: (context: QueryProConfigDb) -> T): T {
        return use {
            this.store.setStore(store.toMutableMap())
            func(it)
        }
    }
}
