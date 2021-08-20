package cn.cloudself.query

enum class QueryFieldType {
    WHERE_FIELD,
    ORDER_BY_FIELD,
    OTHER_FIELD,
}

typealias CreateQueryField<F> = (queryStructure: QueryStructure) -> F

abstract class FinalQueryField<
        T,
        WHERE_FIELD: QueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        ORDER_BY_FIELD: QueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        COLUMN_LIMITER_FILED: QueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        COLUMNS_LIMITER_FILED: QueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
> constructor(private val queryStructure: QueryStructure) {
    protected abstract val clazz: Class<T>
    protected abstract val createField: CreateQueryField<FinalQueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>>
    protected abstract val createColumnLimiterField: CreateQueryField<COLUMN_LIMITER_FILED>
    protected abstract val createColumnsLimiterField: CreateQueryField<COLUMNS_LIMITER_FILED>

    fun limit(limit: Int): FinalQueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED> {
        return createField(queryStructure.copy(limit = limit))
    }

    protected open fun <T>getColumn(field: Field, clazz: Class<T>): List<T?> {
        val newQueryStructure = queryStructure.copy(fields = queryStructure.fields + field)
        val rows = createField(newQueryStructure).runAsMap()
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
        return createColumnsLimiterField(queryStructure)
    }

    fun columnLimiter(): COLUMN_LIMITER_FILED {
        return createColumnLimiterField(queryStructure)
    }

    fun runLimit1(): T? {
        val results = createField(queryStructure.copy(limit = 1)).run()
        return if (results.isEmpty()) null else results[0]
    }

    fun run(): List<T> {
        return QueryStructureResolver.resolve(queryStructure, clazz)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun runAsMap(): List<Map<String, Any>> {
        return QueryStructureResolver.resolve(queryStructure, mapOf<String, Any>().javaClass)
    }

    fun pageable() {
        TODO("分页功能延后")
    }
}

abstract class QueryField<
        T,
        WHERE_FIELD: QueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        ORDER_BY_FIELD: QueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        COLUMN_LIMITER_FILED: QueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
        COLUMNS_LIMITER_FILED: QueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>,
> constructor(protected val queryStructure: QueryStructure)
    : FinalQueryField<T, WHERE_FIELD, ORDER_BY_FIELD, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED>(queryStructure) {
    protected abstract val type: QueryFieldType
    protected abstract val createWhereField: CreateQueryField<WHERE_FIELD>
    protected abstract val createOrderByField: CreateQueryField<ORDER_BY_FIELD>
    override val createField = { qs: QueryStructure -> createWhereField(qs) }

    fun customColumn(column: String) = QueryKeywords(Field(column = column), queryStructure, createWhereField)

    fun and(): WHERE_FIELD {
        if (type != QueryFieldType.WHERE_FIELD) {
            throw RuntimeException("$type can not call and, usage: .orderBy().id.desc().name.asc()")
        }
        @Suppress("UNCHECKED_CAST")
        return this as WHERE_FIELD
    }

    fun or(factor: ((f: WHERE_FIELD) -> WHERE_FIELD)? = null): WHERE_FIELD {
        if (type != QueryFieldType.WHERE_FIELD) {
            throw RuntimeException("$type can not call and, usage: .orderBy().id.desc().name.asc()")
        }

        if (factor == null) {
            return createWhereField(queryStructure.copy(where = queryStructure.where + WhereClause(operator = "or")))
        }

        val vTempQueryStructure = QueryStructure(from = QueryStructureFrom("v_temp"))
        val orWhereClauses = factor(createWhereField(vTempQueryStructure)).queryStructure.where
        val newWhereClause = queryStructure.where + WhereClause(operator = "or", value = orWhereClauses)
        return createWhereField(queryStructure.copy(where = newWhereClause))
    }

    fun andForeignField(vararg fields: QueryField<*, *, *, *, *>): WHERE_FIELD {
        val newWhereClause = queryStructure.where.toMutableList()
        for (field in fields) {
            newWhereClause.addAll(field.queryStructure.where)
        }
        return createWhereField(queryStructure.copy(where = newWhereClause))
    }

    fun orderBy(): ORDER_BY_FIELD {
        return createOrderByField(queryStructure)
    }
}
