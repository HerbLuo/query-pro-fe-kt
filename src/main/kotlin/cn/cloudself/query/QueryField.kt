package cn.cloudself.query

import cn.cloudself.query.exception.IllegalImplements

enum class QueryFieldType {
    WHERE_FIELD,
    ORDER_BY_FIELD,
    OTHER_FIELD,
}

typealias CreateQueryField<F> = (queryStructure: QueryStructure) -> F

@Suppress("PropertyName")
abstract class FinalSelectField<
        T,
        RUN_RES,
        COLUMN_LIMITER_FILED: FinalSelectField<T, RUN_RES, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        COLUMNS_LIMITER_FILED: FinalSelectField<T, RUN_RES, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
> constructor(private val queryStructure: QueryStructure, private val field_clazz: Class<T>) {
    protected abstract val create_column_limiter_field: CreateQueryField<COLUMN_LIMITER_FILED>
    protected abstract val create_columns_limiter_field: CreateQueryField<COLUMNS_LIMITER_FILED>
    @Suppress("FunctionName")
    protected abstract fun create_field(qs: QueryStructure): FinalSelectField<T, RUN_RES, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>

    fun limit(limit: Int): FinalSelectField<T, RUN_RES, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED> {
        return create_field(queryStructure.copy(limit = limit))
    }

    protected open fun <T>getColumn(field: Field, clazz: Class<T>): List<T?> {
        val newQueryStructure = queryStructure.copy(fields = queryStructure.fields + field)
        val rows = create_field(newQueryStructure).runAsMap()
        return rows.map {
            val f = it[field.column]
            if (f != null && clazz.isInstance(f)) {
                @Suppress("UNCHECKED_CAST")
                f as T
            } else {
                null
            }
        }
    }

    fun columnsLimiter(): COLUMNS_LIMITER_FILED {
        return create_columns_limiter_field(queryStructure)
    }

    fun columnLimiter(): COLUMN_LIMITER_FILED {
        return create_column_limiter_field(queryStructure)
    }

    fun count(): Int {
        return 0
    }

    fun runLimit1(): T? {
        val results = create_field(queryStructure.copy(limit = 1)).runAsList()
        return if (results.isEmpty()) null else results[0]
    }

    fun run(): RUN_RES {
        @Suppress("UNCHECKED_CAST")
        return when (queryStructure.action) {
            QueryStructureAction.SELECT -> {
                runAsList() as RUN_RES
            }
            QueryStructureAction.DELETE, QueryStructureAction.UPDATE -> {
                val results = runAsList()
                if (results.isEmpty())
                    throw IllegalImplements("DELETE, UPDATE需返回长度为1的List<Boolean>")
                else
                    results[0] as RUN_RES
            }
        }
    }

    fun runAsList(): List<T> {
        return QueryProConfig.QueryStructureResolver.resolve(queryStructure, field_clazz)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun runAsMap(): List<Map<String, Any?>> {
        return QueryProConfig.QueryStructureResolver.resolve(queryStructure, mutableMapOf<String, Any>().javaClass)
    }

    fun pageable() {
        TODO("分页功能延后")
    }
}

@Suppress("PropertyName")
abstract class QueryField<
        T,
        RUN_RES,
        WHERE_FIELD: QueryField<T, RUN_RES, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        ORDER_BY_FIELD: QueryField<T, RUN_RES, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        COLUMN_LIMITER_FILED: QueryField<T, RUN_RES, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        COLUMNS_LIMITER_FILED: QueryField<T, RUN_RES, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
> constructor(protected val queryStructure: QueryStructure, val field_clazz: Class<T>)
    : FinalSelectField<T, RUN_RES, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>(queryStructure, field_clazz) {
    protected abstract val field_type: QueryFieldType
    protected abstract val create_where_field: CreateQueryField<WHERE_FIELD>
    protected abstract val create_order_by_field: CreateQueryField<ORDER_BY_FIELD>
    override fun create_field(qs: QueryStructure) = create_where_field(qs)

    fun customColumn(column: String) = QueryKeywords(Field(column = column), queryStructure, create_where_field)

    fun and(): WHERE_FIELD {
        if (field_type != QueryFieldType.WHERE_FIELD) {
            throw RuntimeException("$field_type can not call and, usage: .orderBy().id.desc().name.asc()")
        }
        @Suppress("UNCHECKED_CAST")
        return this as WHERE_FIELD
    }

    fun or(factor: ((f: WHERE_FIELD) -> WHERE_FIELD)? = null): WHERE_FIELD {
        if (field_type != QueryFieldType.WHERE_FIELD) {
            throw RuntimeException("$field_type can not call and, usage: .orderBy().id.desc().name.asc()")
        }

        if (factor == null) {
            return create_where_field(queryStructure.copy(where = queryStructure.where + WhereClause(operator = "or")))
        }

        val vTempQueryStructure = QueryStructure(from = QueryStructureFrom("v_temp")) // v_temp会消失, 只取where
        val orWhereClauses = factor(create_where_field(vTempQueryStructure)).queryStructure.where
        val newWhereClause = queryStructure.where + WhereClause(operator = "or", value = orWhereClauses)
        return create_where_field(queryStructure.copy(where = newWhereClause))
    }

    fun andForeignField(vararg fields: QueryField<*, *, *, *, *, *>): WHERE_FIELD {
        val newWhereClause = queryStructure.where.toMutableList()
        for (field in fields) {
            newWhereClause.addAll(field.queryStructure.where)
        }
        return create_where_field(queryStructure.copy(where = newWhereClause))
    }

    fun orderBy(): ORDER_BY_FIELD {
        return create_order_by_field(queryStructure)
    }
}

open class UpdateField<UPDATE_BY_FIELD: QueryField<*, *, *, *, *, *>>(
    private val queryStructure: QueryStructure,
    private val createUpdateByField: CreateQueryField<UPDATE_BY_FIELD>,
) {
    @JvmField
    val where = createUpdateByField(queryStructure.copy(action = QueryStructureAction.UPDATE))

    fun run(): Boolean = createUpdateByField(queryStructure).run() as Boolean
}
