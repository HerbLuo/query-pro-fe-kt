package cn.cloudself.query

import cn.cloudself.query.exception.IllegalParameters
import cn.cloudself.query.util.onlyAZaz_
import org.jetbrains.annotations.Contract

class QueryKeywords<F : QueryField<*, *, *, *, *, *>>(
    private val field: Field,
    private val queryStructure: QueryStructure,
    private val createQueryField: CreateQueryField<F>,
) {
    val `is` = this
    val not = QueryWithNotKeywords(field, queryStructure, createQueryField)
    val ignoreCase = QueryIgnoreCaseKeywords(field, queryStructure, createQueryField)

    @Contract(pure = true)
    fun `is`() = this
    @Contract(pure = true)
    fun not() = not
    @Contract(pure = true)
    fun ignoreCase() = ignoreCase

    @Contract(pure = true)
    fun equalsTo(value: Any) = with(WhereClause(field, "=", value))
    @Contract(pure = true)
    fun between(start: Any, end: Any) = with(WhereClause(field, "between", arrayOf(start, end)))
    @Contract(pure = true)
    fun lessThan(value: Any) = with(WhereClause(field, "<", value))
    @Contract(pure = true)
    fun lessThanOrEqual(value: Any) = with(WhereClause(field, "<=", value))
    @Contract(pure = true)
    fun graterThan(value: Any) = with(WhereClause(field, ">", value))
    @Contract(pure = true)
    fun graterThanOrEqual(value: Any) = with(WhereClause(field, ">=", value))
    @Contract(pure = true)
    fun like(str: String) = with(WhereClause(field, "like", str))
//    @Contract(pure = true)
//    fun `in`(vararg values: Any) = if (values.isEmpty()) throw IllegalParameters("in查询不能设置空参数") else with(WhereClause(field, "in", values))
    @Contract(pure = true)
    fun `in`(vararg values: Any) =
        if (values.isEmpty()) {
            val table = field.table
            with(WhereClause(null, operator = "", sql = "false/* WARN: ${if (table == null) "" else onlyAZaz_(table)}${if (table == null) "" else "."}${onlyAZaz_(field.column)} in empty*/"))
        }
        else with(WhereClause(field, "in", values))
//    @Contract(pure = true)
//    fun `in`(values: List<Any>) = if (values.isEmpty()) throw IllegalParameters("in查询不能设置空参数") else with(WhereClause(field, "in", values))
    @Contract(pure = true)
    fun nul() = with(WhereClause(field = field, operator = "is null"))
    @Contract(pure = true)
    fun isNull() = with(WhereClause(field = field, operator = "is null"))
    @Contract(pure = true)
    fun isNotNull() = with(WhereClause(field = field, operator = "is not null"))
    @Contract(pure = true)
    fun sql(sql: String) = with(WhereClause(field = field, operator = "", sql = sql))

    private fun with(whereClause: WhereClause) = createQueryField(queryStructure.copy(where = queryStructure.where + whereClause))
}

class QueryWithNotKeywords<F : QueryField<*, *, *, *, *, *>>(
    private val field: Field,
    private val queryStructure: QueryStructure,
    private val createQueryField: CreateQueryField<F>,
) {
    @Contract(pure = true)
    fun equalsTo(value: Any) = with(WhereClause(field, "<>", value))
    @Contract(pure = true)
    fun between(start: Any, end: Any) = with(WhereClause(field, "not between", arrayOf(start, end)))
    @Contract(pure = true)
    fun like(str: String) = with(WhereClause(field, "not like", str))
    @Contract(pure = true)
    fun `in`(vararg values: Any) = if (values.isEmpty()) throw IllegalParameters("not in查询不能设置空参数") else with(WhereClause(field, "not in", values))
    @Contract(pure = true)
    fun nul() = with(WhereClause(field = field, operator = "is not null"))

    private fun with(whereClause: WhereClause) = createQueryField(queryStructure.copy(where = queryStructure.where + whereClause))
}

class QueryIgnoreCaseKeywords<F : QueryField<*, *, *, *, *, *>>(
    private val field: Field,
    private val queryStructure: QueryStructure,
    private val createQueryField: CreateQueryField<F>,
) {
    @Contract(pure = true)
    fun equalsTo(value: Any) = with(WhereClause(upperField(field), "=", value, WhereClauseCommands.UPPER_CASE))
    @Contract(pure = true)
    fun like(str: String) = with(WhereClause(upperField(field), "like", str, WhereClauseCommands.UPPER_CASE))
    @Contract(pure = true)
    fun `in`(vararg values: Any) =
        if (values.isEmpty()) throw IllegalParameters("in查询+ignore case不能设置空参数")
        else with(WhereClause(upperField(field), "in", values, WhereClauseCommands.UPPER_CASE))

    private fun upperField(field: Field) = Field(table = field.table, column = field.column)
    private fun with(whereClause: WhereClause) = createQueryField(queryStructure.copy(where = queryStructure.where + whereClause))
}

class QueryOrderByKeywords<F: QueryField<*, *, *, *, *, *>>(
    private val field: Field,
    private val queryStructure: QueryStructure,
    private val createQueryField: CreateQueryField<F>,
) {
    @Contract(pure = true)
    fun asc() = with(OrderByClause(field, "asc"))
    @Contract(pure = true)
    fun desc() = with(OrderByClause(field, "desc"))

    private fun with(orderBy: OrderByClause) = createQueryField(queryStructure.copy(orderBy = queryStructure.orderBy + orderBy))
}
