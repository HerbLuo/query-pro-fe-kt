package cn.cloudself.query

import cn.cloudself.query.structure_reolsver.parseClass

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
) {
    /**
     * 查询操作
     */
    fun selectBy() = createSelectByField(queryStructure.copy(action = QueryStructureAction.SELECT))

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
    fun insert(collection: Collection<T>) = QueryProConfig.final.queryStructureResolver().insert(collection, clazz) as List<ID?>
}
