package cn.cloudself.query

import cn.cloudself.query.exception.IllegalCall
import cn.cloudself.query.exception.IllegalImplements
import java.util.*

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
        return limit(0, limit)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun limit(start: Int, limit: Int): FinalSelectField<T, RUN_RES, COLUMN_LIMITER_FILED, COLUMNS_LIMITER_FILED> {
        return create_field(queryStructure.copy(limit = start to limit))
    }

    protected open fun <T: Any>getColumn(field: Field, clazz: Class<T>): List<T?> {
        val newQueryStructure = queryStructure.copy(fields = queryStructure.fields + field)
        val rows = create_field(newQueryStructure).runAsMap()
        return rows.map {
            val f = it[field.column] ?: return@map null

            val fieldJavaClass = f.javaClass
            if (clazz.isAssignableFrom(f.javaClass)) {
                @Suppress("UNCHECKED_CAST")
                return@map f as T
            }
            val targetPrimitiveType = clazz.kotlin.javaPrimitiveType ?: return@map null
            val objPrimitiveType = fieldJavaClass.kotlin.javaPrimitiveType
            if (targetPrimitiveType == objPrimitiveType) {
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

    @Suppress("MemberVisibilityCanBePrivate")
    fun count(): Int {
        if (queryStructure.action != QueryStructureAction.SELECT) {
            throw IllegalCall("非SELECT语句不能使用count方法")
        }
        val queryStructureForCount = queryStructure.copy(fields = listOf(Field(column = "count(*)")))

        return QueryProConfig.final.queryStructureResolver().resolve(queryStructureForCount, Int::class.java)[0]
    }

    fun runLimit1Opt(): Optional<T> {
        return Optional.ofNullable(runLimit1())
    }

    fun runLimit1(): T? {
        val results = create_field(queryStructure.copy(limit = 0 to 1)).runAsList()
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
            QueryStructureAction.INSERT -> {
                throw IllegalCall("run方法暂不支持INSERT")
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun runAsList(): List<T> {
        return QueryProConfig.final.queryStructureResolver().resolve(preRun(queryStructure), field_clazz)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun runAsMap(): List<Map<String, Any?>> {
        return QueryProConfig.final.queryStructureResolver().resolve(preRun(queryStructure), mutableMapOf<String, Any>().javaClass)
    }

    fun pageable(): Pageable<T> {
        return Pageable.create(
            { count() },
            { start, limit -> QueryProConfig.final.queryStructureResolver().resolve(queryStructure.copy(limit = start to limit), field_clazz) }
        )
    }

    private fun preRun(qs: QueryStructure): QueryStructure {
        var queryStructure = qs
        if (QueryProConfig.final.logicDelete()) {
            queryStructure = if (queryStructure.action == QueryStructureAction.DELETE) {
                val update = Update(data = mutableMapOf("deleted" to true), override = false)
                queryStructure.copy(action = QueryStructureAction.UPDATE, update = update)
            } else {
                val tables = queryStructure.where.map { it.field?.table }.distinct()
                val whereClauses = tables.map { WhereClause(Field(it, "deleted"), "=", false) }
                queryStructure.copy(where = queryStructure.where + whereClauses)
            }
        }
        return queryStructure
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

    @JvmOverloads
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
    val where = createUpdateByField(queryStructure.copy(action = QueryStructureAction.UPDATE))

    fun where() = where

    fun run(): Boolean = createUpdateByField(queryStructure).run() as Boolean
}
