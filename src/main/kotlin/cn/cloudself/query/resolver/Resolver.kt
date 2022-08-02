package cn.cloudself.query.resolver

import cn.cloudself.query.*
import cn.cloudself.query.config.HashMapStore
import cn.cloudself.query.config.QueryProConfig
import cn.cloudself.query.exception.IllegalCall
import cn.cloudself.query.util.LogFactory
import cn.cloudself.query.util.Result
import cn.cloudself.query.util.parseClass

class Resolver private constructor(
    private val clazz: Class<*>? = null,
    private val queryStructure: QueryStructure? = null,
    private val payload: (() -> QueryPayload)? = null,
    private val store: (() -> HashMapStore)? = null,
) {
    private val logger = LogFactory.getLog(Resolver::class.java)

    companion object {
        fun create(store: () -> HashMapStore): Resolver {
            return Resolver(store = store)
        }
        fun create(clazz: Class<*>, queryStructure: QueryStructure, payload: () -> QueryPayload): Resolver {
            return Resolver(clazz, queryStructure, payload)
        }
    }

    fun <T> resolve(clazz: Class<T>): List<T> {
        return use {
            if (it == null) {
                throw IllegalCall("resolve(Class)方法必须传入queryStructure")
            }
            resolve(it, clazz)
        }
    }

    fun <R> use(resolve: IQueryStructureResolver.(QueryStructure?) -> R): R {
        preRun()
        val transformedQueryStructure = if (queryStructure == null) null else beforeExecLifecycleCallback(queryStructure).getOrElse {
            throw it
        }
        val config = payload?.let { it().config.toMap() }
            ?: store?.let { it().toMap() }
            ?: throw IllegalCall("payload, store必须存在一个。")
        val result = QueryProConfig.final.queryStructureResolver().withConfig(config) {
            resolve(it, transformedQueryStructure)
        }
        return afterExecLifecycleCallback(result).getOrElse {
            throw it
        }
    }

    private fun preRun() {
        if (QueryProConfig.final.logicDelete()) {
            if (queryStructure != null) {
                val logicDeleteField = QueryProConfig.final.logicDeleteField()
                if (queryStructure.action == QueryStructureAction.DELETE) {
                    val update = Update(data = mutableMapOf(logicDeleteField to true), override = false)
                    queryStructure.action = QueryStructureAction.UPDATE
                    queryStructure.update = update
                } else if (clazz != null) {
                    val mainTable = queryStructure.from.main
                    val hasDeletedField = parseClass(clazz).columns[logicDeleteField] != null
                    if (hasDeletedField) {
                        val hasOrClause = queryStructure.where.find { it.operator == OP_OR } != null
                        val noDeletedWhereClause = WhereClause(Field(mainTable, logicDeleteField), "=", false)
                        if (hasOrClause) {
                            queryStructure.where = listOf(WhereClause(operator = "("))  + queryStructure.where + WhereClause(operator = ")") + noDeletedWhereClause
                        } else {
                            queryStructure.where = queryStructure.where + noDeletedWhereClause
                        }
                    }
                }
            }
        }
    }

    private fun beforeExecLifecycleCallback(queryStructure: QueryStructure): Result<QueryStructure, Throwable> {
        val nonNullClazz = clazz ?: return Result.ok(queryStructure)
        val nonNullPayload = payload ?: return Result.ok(queryStructure)

        var transformedQueryStructure = queryStructure

        val transformers = QueryProConfig.final.lifecycle().beforeExecTransformers
        for (transformer in transformers) {
            transformedQueryStructure = transformer(nonNullClazz, transformedQueryStructure, nonNullPayload()).getOrElse {
                logger.warn("beforeExec钩子阻止了本次操作" as Any, it)
                return Result.err(it)
            }
        }

        return Result.ok(transformedQueryStructure)
    }

    private fun <R> afterExecLifecycleCallback(result: R): Result<R, Throwable> {
        val nonNullQueryStructure = queryStructure ?: return Result.ok(result)
        val nonNullPayload = payload ?: return Result.ok(result)

        @Suppress("KotlinConstantConditions") var transformedResult: Any = result ?: return Result.ok(result)

        val transformers = QueryProConfig.final.lifecycle().afterExecTransformers
        for (transformer in transformers) {
            transformedResult = transformer(transformedResult, nonNullQueryStructure, nonNullPayload()).getOrElse {
                logger.warn("beforeExec钩子阻止了本次操作" as Any, it)
                return Result.err(it)
            }
        }

        @Suppress("UNCHECKED_CAST")
        return Result.ok(transformedResult as R)
    }
}