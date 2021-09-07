package cn.cloudself.query

import cn.cloudself.query.structure_reolsver.parseClass

typealias CreateQuery<QUERY> = (queryStructure: QueryStructure) -> QUERY

class QueryPro<
        T,
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
     * 更新操作 updateSet(Apple(id = 2021, name = "iPhone13", type = null)).run()
     * 如果 需要更新的值为null, 则跳过该字段不更新
     * 如确实需要更新, 使用[updateSetOverride]
     */
    fun updateSet(obj: T) = updateSet(obj, false)

    /**
     * 更新操作 updateSetOverride(Apple(id = 2021, name = "iPhone13", type = null)).run()
     * 注意如果更新的值为null, 该方法会将其更新为null
     * 如不想更新为null的字段, 使用[updateSet]
     */
    fun updateSetOverride(obj: T) = updateSet(obj, true)

    private fun updateSet(obj: T, override: Boolean): UpdateField<UPDATE_BY_FIELD> {
        val update = Update(data = obj, override = override, id = parseClass(clazz).idColumn)
        return UpdateField(queryStructure.copy(action = QueryStructureAction.UPDATE, update = update), createUpdateByField)
    }

    /**
     * 删除操作
     */
    fun deleteBy() = createDeleteByField(queryStructure.copy(action = QueryStructureAction.DELETE))
}
