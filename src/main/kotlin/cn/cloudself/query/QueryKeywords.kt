package cn.cloudself.query

class QueryKeywords<F : QueryField<*, *, *, *, *, *>>(
    private val field: Field,
    private val queryStructure: QueryStructure,
    private val createQueryField: CreateQueryField<F>,
) {
    val `is` = this
    val not = QueryWithNotKeywords(field, queryStructure, createQueryField)
    val ignoreCase = QueryIgnoreCaseKeywords(field, queryStructure, createQueryField)

    fun `is`() = this
    fun not() = not
    fun ignoreCase() = ignoreCase

    fun equalsTo(value: Any) = with(WhereClause(field, "=", value))
    fun between(start: Any, end: Any) = with(WhereClause(field, "between", arrayOf(start, end)))
    fun lessThan(value: Any) = with(WhereClause(field, "<", value))
    fun lessThanOrEqual(value: Any) = with(WhereClause(field, "<=", value))
    fun graterThan(value: Any) = with(WhereClause(field, ">", value))
    fun graterThanOrEqual(value: Any) = with(WhereClause(field, ">=", value))
    fun like(str: String) = with(WhereClause(field, "like", str))
    fun `in`(vararg values: Any) = with(WhereClause(field, "in", values))
    fun nul() = with(WhereClause(field = field, operator = "is null"))
    fun isNull() = with(WhereClause(field = field, operator = "is null"))
    fun isNotNull() = with(WhereClause(field = field, operator = "is not null"))

    private fun with(whereClause: WhereClause) = createQueryField(queryStructure.copy(where = queryStructure.where + whereClause))
}

class QueryWithNotKeywords<F : QueryField<*, *, *, *, *, *>>(
    private val field: Field,
    private val queryStructure: QueryStructure,
    private val createQueryField: CreateQueryField<F>,
) {
    fun equalsTo(value: Any) = with(WhereClause(field, "<>", value))
    fun between(start: Any, end: Any) = with(WhereClause(field, "not between", arrayOf(start, end)))
    fun like(str: String) = with(WhereClause(field, "not like", str))
    fun `in`(vararg values: Any) = with(WhereClause(field, "not in", values))
    fun nul() = with(WhereClause(field = field, operator = "is not null"))

    private fun with(whereClause: WhereClause) = createQueryField(queryStructure.copy(where = queryStructure.where + whereClause))
}

class QueryIgnoreCaseKeywords<F : QueryField<*, *, *, *, *, *>>(
    private val field: Field,
    private val queryStructure: QueryStructure,
    private val createQueryField: CreateQueryField<F>,
) {
    fun equalsTo(value: Any) = with(WhereClause(upperField(field), "=", value, WhereClauseCommands.UPPER_CASE))
    fun like(str: String) = with(WhereClause(upperField(field), "like", str, WhereClauseCommands.UPPER_CASE))
    fun `in`(vararg values: Any) = with(WhereClause(upperField(field), "in", values, WhereClauseCommands.UPPER_CASE))

    private fun upperField(field: Field) = Field(table = field.table, column = field.column)
    private fun with(whereClause: WhereClause) = createQueryField(queryStructure.copy(where = queryStructure.where + whereClause))
}

class QueryOrderByKeywords<F: QueryField<*, *, *, *, *, *>>(
    private val field: Field,
    private val queryStructure: QueryStructure,
    private val createQueryField: CreateQueryField<F>,
) {
    fun asc() = with(OrderByClause(field, "asc"))
    fun desc() = with(OrderByClause(field, "desc"))

    private fun with(orderBy: OrderByClause) = createQueryField(queryStructure.copy(orderBy = queryStructure.orderBy + orderBy))
}
