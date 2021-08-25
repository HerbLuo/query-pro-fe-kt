package cn.cloudself.query

typealias CreateQuery<QUERY> = (queryStructure: QueryStructure) -> QUERY

class QueryPro<
    WHERE_FIELD: QueryField<*, *, *, *, *>,
    ORDER_BY_FIELD: QueryField<*, *, *, *, *>
> constructor(
    private val queryStructure: QueryStructure,
    private val createWhereField: CreateQueryField<WHERE_FIELD>,
    private val createOrderByField: CreateQueryField<ORDER_BY_FIELD>,
) {
    fun selectBy() = createWhereField(queryStructure.copy(action = QueryStructureAction.SELECT))

    fun selectOneBy() = createWhereField(queryStructure.copy(action = QueryStructureAction.SELECT, limit = 1))

    fun orderBy() = createOrderByField(queryStructure.copy(action = QueryStructureAction.SELECT))

    fun updateBy() = createWhereField(queryStructure.copy(action = QueryStructureAction.UPDATE))

    fun deleteBy() = createWhereField(queryStructure.copy(action = QueryStructureAction.DELETE))
}
