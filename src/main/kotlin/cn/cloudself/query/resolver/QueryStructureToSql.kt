package cn.cloudself.query.resolver

import cn.cloudself.query.*
import cn.cloudself.query.config.QueryProConfig
import cn.cloudself.query.exception.MissingParameter
import cn.cloudself.query.exception.UnSupportException
import cn.cloudself.query.util.ObjectUtil
import java.lang.StringBuilder

class QueryStructureToSql(
    private val qs: QueryStructure,
) {
    private val beautify = QueryProConfig.final.beautifySql()
    private val sql = StringBuilder()
    private val indexedParams = mutableListOf<Any?>()

    fun toSqlWithIndexedParams(): Pair<String, List<Any?>> {
        val action = qs.action
        sql.append(action.name, ' ')
        if (action == QueryStructureAction.SELECT) {
            buildFields(qs.fields)
            sql.append(if (beautify) '\n' else ' ')
        }
        if (action == QueryStructureAction.SELECT || action == QueryStructureAction.DELETE) {
            sql.append("FROM ")
        }
        buildFromClause(qs.from)
        var idWhereClause: WhereClause? = null
        if (action == QueryStructureAction.UPDATE) {
            idWhereClause = buildUpdateSetField(qs.update ?: throw MissingParameter("updateSet缺少参数, 参考.updateSet(obj)"))
        }
        val wheres = if (idWhereClause == null) {
            qs.where
        } else {
            qs.where + idWhereClause
        }
        if (action == QueryStructureAction.UPDATE && wheres.isEmpty()) {
            throw MissingParameter("updateSet缺少参数, 需指定id字段或者where条件")
        }
        buildWheresClause(wheres)
        buildOrderByClause(qs.orderBy)
        buildLimitClause(qs.limit)

        val sqlWithIndexedParams = sql.toString() to indexedParams
        beforeReturnForTest?.let { it(sqlWithIndexedParams) }
        return sqlWithIndexedParams
    }

    private fun buildField(field: Field?, whereClauseUpper: Boolean) {
        if (field == null) {
            return
        }
        val upper = if (field.commands == FieldCommands.UPPER_CASE)
            true
        else
            whereClauseUpper
        if (upper) {
            sql.append("UPPER(")
        }
        if (field.table != null) {
            sql.append('`', field.table, '`', '.')
        }
        val column = field.column
        val isActualColumn = "count(*)" != column
        if (isActualColumn) {
            sql.append('`', column, '`')
        } else {
            sql.append(column)
        }
        if (upper) {
            sql.append(')')
        }
    }

    private fun buildValue(v: Any?, upper: Boolean) {
        if (v != null) {
            sql.append(if (upper) "UPPER(?)" else '?')
            indexedParams.add(v)
        }
    }

    private fun buildFields(fields: List<Field>) {
        if (fields.isEmpty()) {
            if (qs.from.joins.isNotEmpty()) {
                sql.append('`', qs.from.main, '`', ".*")
            } else {
                sql.append('*')
            }
            return
        }

        val lastIndexOfFields = fields.size - 1
        for ((i, field) in fields.withIndex()) {
            buildField(field, false)
            if (i != lastIndexOfFields) {
                sql.append(",")
                sql.append(if (beautify) "\n       " else ' ')
            }
        }
    }

    private fun buildFromClause(from: QueryStructureFrom) {
        sql.append('`', from.main, '`')
        for (joiner in from.joins) {
            sql.append(if (beautify) "\n    " else ' ')
            sql.append(when (joiner.type) {
                JoinType.LEFT_JOIN -> "LEFT JOIN "
            })
            sql.append('`', joiner.table, '`')
            sql.append(" ON ")
            val lastIndexOfJoinOn = joiner.on.size - 1
            for ((i, joinOn) in joiner.on.withIndex()) {
                buildField(joinOn.left, false)
                sql.append(" = ")
                buildField(joinOn.right, false)
                if (i != lastIndexOfJoinOn) {
                    sql.append(" AND ")
                }
            }
        }
    }

    private fun buildUpdateSetField(update: Update): WhereClause? {
        sql.append(" SET")
        val data = update.data ?: throw MissingParameter(".updateSet(obj): obj不能为null")
        val override = update.override

        var first = true
        val columns = ObjectUtil.toSequence(data)
        val idColumn = update.id
        var idWhereClause: WhereClause? = null
        for (column in columns) {
            val value = column.value
            val field = column.dbName
            if (!override && value == null) {
                continue
            }
            if (field == idColumn) {
                idWhereClause = WhereClause(Field(table = qs.from.main, column = idColumn), "=", value)
                continue
            }
            if (!first) {
                sql.append(",")
            }
            sql.append(" `", field, "` = ?")
            indexedParams.add(value ?: NULL)
            first = false
        }
        if (first) {
            throw MissingParameter(".updateSet()没有设置内容或设置的内容全为空")
        }
        return idWhereClause
    }

    private fun buildWheresClause(wheres: List<WhereClause>) {
        fun parseWhereClause(whereClause: WhereClause) {
            val upper = whereClause.commands == WhereClauseCommands.UPPER_CASE
            val field = whereClause.field
            val operator = whereClause.operator
            val value = whereClause.value
            val whereSql = whereClause.sql

            buildField(field, upper)
            sql.append(' ', operator, ' ')

            if (value is List<*> || value is Array<*>) {
                sql.append('(')
                val lastIndexOfValues: Int
                val valueIndexIter: Iterable<IndexedValue<Any?>>
                when (value) {
                    is List<*> -> {
                        lastIndexOfValues = value.size - 1
                        valueIndexIter = value.withIndex()
                    }
                    is Array<*> -> {
                        lastIndexOfValues = value.size - 1
                        valueIndexIter = value.withIndex()
                    }
                    else -> throw Error("不可达1")
                }
                for ((vi, v) in valueIndexIter) {
                    if (v is WhereClause) {
                        parseWhereClause(v)
                    } else {
                        buildValue(v, upper)
                    }
                    if (vi != lastIndexOfValues) {
                        sql.append(when (operator) {
                            "in" -> ", "
                            "not in" -> ", "
                            "between" -> " AND "
                            "not between" -> " AND "
                            OP_OR -> " AND "
                            else -> throw UnSupportException("未知的运算符{0}", operator)
                        })
                    }
                }
                sql.append(')')
            } else {
                if (whereSql != null) {
                    sql.append(whereSql)
                } else {
                    buildValue(value, upper)
                }
            }
        }
        if (wheres.isEmpty()) {
            return
        }

        sql.append(if (beautify) '\n' else ' ')
        sql.append("WHERE ")
        val lastIndexOfWheres = wheres.size - 1
        var leftParCount = 0
        for ((i, whereClause) in wheres.withIndex()) {
            val operator = whereClause.operator

            if (beautify) {
                if (operator == OP_OR) {
                    sql.append("\n ")
                }
                if (operator == "(") {
                    leftParCount++
                }
                if (operator == ")") {
                    leftParCount--
                }
            }

            parseWhereClause(whereClause)

            if (lastIndexOfWheres != i &&
                operator != "(" &&
                wheres[i + 1].operator != ")" &&
                operator != OP_OR &&
                wheres[i + 1].operator != OP_OR
            ) {
                sql.append(if (beautify && leftParCount == 0) "\n  " else ' ')
                sql.append("AND ")
            }
        }
    }

    private fun buildOrderByClause(orderBys: List<OrderByClause>) {
        if (orderBys.isEmpty()) {
            return
        }
        sql.append(if (beautify) '\n' else ' ')
        sql.append("ORDER BY ")
        val lastIndexOfOrderBy = orderBys.size - 1
        for ((i, orderBy) in orderBys.withIndex()) {
            buildField(orderBy.field, false)
            sql.append(' ')
            sql.append(when (orderBy.operator) {
                "desc" -> "DESC"
                "asc" -> "ASC"
                "" -> "ASC"
                else -> throw UnSupportException("不支持的order by操作符{0}", orderBy.operator)
            })
            if (i != lastIndexOfOrderBy) {
                sql.append(", ")
            }
        }
    }

    private fun buildLimitClause(limit: Pair<Int, Int>?) {
        if (limit == null) {
            return
        }
        sql.append(if (beautify) '\n' else ' ')
        sql.append("LIMIT ")
        if (limit.first != 0) {
            sql.append(limit.first, ", ")
        }
        sql.append(limit.second)
    }

    companion object {
        internal var beforeReturnForTest: ((Pair<String, List<Any?>>) -> Unit)? = null
    }
}
