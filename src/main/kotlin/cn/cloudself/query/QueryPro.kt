package cn.cloudself.query

typealias CreateQuery<QUERY> = (queryStructure: QueryStructure) -> QUERY

class QueryPro<
    SELECT_BY_FIELD: QueryField<*, *, *, *, *>,
    ORDER_BY_FIELD: QueryField<*, *, *, *, *>,
    UPDATE_BY_FIELD: QueryField<*, *, *, *, *>,
    DELETE_BY_FIELD: QueryField<*, *, *, *, *>
> constructor(
    private val queryStructure: QueryStructure,
    private val createSelectByField: CreateQueryField<SELECT_BY_FIELD>,
    private val createOrderByField: CreateQueryField<ORDER_BY_FIELD>,
    private val createUpdateByField: CreateQueryField<UPDATE_BY_FIELD>,
    private val createDeleteByField: CreateQueryField<DELETE_BY_FIELD>,
) {
    fun selectBy() = createSelectByField(queryStructure.copy(action = QueryStructureAction.SELECT))

    fun orderBy() = createOrderByField(queryStructure.copy(action = QueryStructureAction.SELECT))

    fun updateBy() = createUpdateByField(queryStructure.copy(action = QueryStructureAction.UPDATE))

    fun deleteBy() = createDeleteByField(queryStructure.copy(action = QueryStructureAction.DELETE))
}
