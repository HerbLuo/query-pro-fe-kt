package cn.cloudself.query

import cn.cloudself.query.exception.IllegalCall
import cn.cloudself.query.util.ObjectUtil
import cn.cloudself.query.util.PureContract
import cn.cloudself.query.util.parseClass
import org.jetbrains.annotations.Contract
import javax.sql.DataSource

typealias CreateQuery<QUERY> = (queryStructure: QueryStructure) -> QUERY

open class QueryPro<
        T: Any,
        ID: Any,
        SELECT_BY_FIELD: QueryField<*, *, *, *, *, *>,
        ORDER_BY_FIELD: QueryField<*, *, *, *, *, *>,
        UPDATE_SET_FIELD: UpdateField<UPDATE_BY_FIELD>,
        UPDATE_BY_FIELD: QueryField<*, *, *, *, *, *>,
        DELETE_BY_FIELD: QueryField<*, *, *, *, *, *>
> constructor(
    private val clazz: Class<T>,
    private val queryStructure: QueryStructure,
    private val createSelectByField: CreateQueryField<SELECT_BY_FIELD>,
    private val createOrderByField: CreateQueryField<ORDER_BY_FIELD>,
    private val createUpdateSetField: CreateQueryField<UPDATE_SET_FIELD>,
    private val createUpdateByField: CreateQueryField<UPDATE_BY_FIELD>,
    private val createDeleteByField: CreateQueryField<DELETE_BY_FIELD>,
): IQueryProConfigDbWriteable {
    private val store = CodeStore(clazz)
    val payload = QueryPayload(store)

    /**
     * 查询操作
     */
    fun selectAll() = selectBy()

    /**
     * 查询操作
     */
    fun selectBy() = createSelectByField(queryStructure.apply { action = QueryStructureAction.SELECT })

    fun selectByObj(obj: T): SELECT_BY_FIELD {
        val parsedClass = parseClass(clazz)

        val javaNameMapDbName = mutableMapOf<String, String>()
        for ((_, column) in parsedClass.columns) {
            javaNameMapDbName[column.javaName] = column.dbName
        }

        val whereClauses = ObjectUtil.toSequence(obj).toList().also { println(it) }
            .map { column -> column.dbName to column.value }
            .filter { (_, v) -> v != null }
            .map { (k, v) ->
                WhereClause(Field(null, k), "=", v)
            }

        val newQueryStructure = queryStructure.copy(
            action = QueryStructureAction.SELECT,
            where = whereClauses,
            limit = 0 to 1
        )
        return createSelectByField(newQueryStructure)
    }

    /**
     * 使用主键查询
     */
    fun selectByPrimaryKey(value: Any): T? {
        val idColumn = parseClass(clazz).idColumn ?: throw IllegalCall("Class {0} 没有找到主键", clazz.name)
        val whereClause = WhereClause(Field(null, idColumn), "=", value)
        val newQueryStructure = queryStructure.copy(
            action = QueryStructureAction.SELECT,
            where = listOf(whereClause),
            limit = 0 to 1
        )
        @Suppress("UNCHECKED_CAST")
        return createSelectByField(newQueryStructure).runLimit1() as T?
    }

    /**
     * 排序操作
     */
    fun orderBy() = createOrderByField(queryStructure.copy(action = QueryStructureAction.SELECT))

    /**
     * 更新操作 例子：updateSet().kee("new-key").where.id.equalsTo(1).run()
     * 注意如果要更新的值传入null(例子中是kee), 则会报错,
     * 如确实需要更新为null, 使用
     * kotlin: updateSet().kee(NULL).where.id.equalsTo(1).run()
     * java: updateSet().kee(QueryProConstKt.NULL).where.id().equalsTo(1).run()
     */
    @PureContract
    @Contract(pure = true)
    fun updateSet() = createUpdateSetField(queryStructure.copy(action = QueryStructureAction.UPDATE, update = Update(
        data = mutableMapOf<String, Any>(), override = false, id = parseClass(clazz).idColumn
    )))

    /**
     * 更新操作
     *
     * updateSet(Apple(id = 2021, name = "iPhone13", type = null)).run()
     * 如果 需要更新的值为null, 则跳过该字段不更新
     * 如确实需要更新, 使用
     * updateSet(Apple(id = 2021, name = "iPhone13", type = null), true).run()
     * 如果需要更新的值更新的值为null, 会将其更新为null
     */
    @PureContract
    @Contract(pure = true)
    @JvmOverloads
    fun updateSet(obj: T, override: @ParameterName("override") Boolean = false): UpdateField<UPDATE_BY_FIELD> {
        val update = Update(data = obj, override = override, id = parseClass(clazz).idColumn)
        return UpdateField(queryStructure.copy(action = QueryStructureAction.UPDATE, update = update), createUpdateByField)
    }

    /**
     * 删除操作
     */
    fun deleteBy() = createDeleteByField(queryStructure.copy(action = QueryStructureAction.DELETE))

    /**
     * 使用主键删除
     */
    fun deleteByPrimaryKey(keyValue: Any): Boolean =
        createDeleteByField(queryStructure.copy(
            action = QueryStructureAction.DELETE,
            where = listOf(WhereClause(Field(null, parseClass(clazz).idColumn ?: "id"), "=", keyValue))
        )).run() == true

    /**
     * 插入操作
     */
    @Suppress("UNCHECKED_CAST")
    fun insert(obj: T): ID? = insert(listOf(obj)).getOrNull(0)

    /**
     * 插入操作
     */
    @Suppress("UNCHECKED_CAST")
    fun insert(vararg objs: T) = insert(listOf(*objs))

    /**
     * 批量插入
     */
    @Suppress("UNCHECKED_CAST")
    fun insert(collection: Collection<T>) = withConfig(store) {
        insert(collection, clazz) as List<ID?>
    }

    @SafeVarargs
    @Suppress("UNCHECKED_CAST")
    fun insert(vararg objs: Map<String, Any?>) = withConfig(store) {
        insert(listOf(*objs), clazz) as List<ID?>
    }

    override fun setDataSource(dataSource: DataSource) = setConfig("dataSource", dataSource)
    override fun setBeautifySql(beautifySql: Boolean) = setConfig("beautifySql", beautifySql)
    override fun setPrintSql(printSql: Boolean) = setConfig("printSql", printSql)
    override fun setPrintCallByInfo(printCallByInfo: Boolean) = setConfig("printCallByInfo", printCallByInfo)
    override fun setPrintResult(printResult: Boolean) = setConfig("printResult", printResult)
    override fun setDryRun(dryRun: Boolean) = setConfig("dryRun", dryRun)
    override fun setQueryProFieldComment(queryProFieldComment: Boolean) = setConfig("queryProFieldComment", queryProFieldComment)
    override fun setLogicDelete(logicDelete: Boolean) = setConfig("logicDelete", logicDelete)
    override fun setLogicDeleteField(logicDeleteField: String) = setConfig("logicDeleteField", logicDeleteField)
    override fun setQueryStructureResolver(queryStructureResolver: IQueryStructureResolver) = setConfig("queryStructureResolver", queryStructureResolver)

    private fun setConfig(key: String, value: Any?) = this.also { store.set(key, value) }
}
