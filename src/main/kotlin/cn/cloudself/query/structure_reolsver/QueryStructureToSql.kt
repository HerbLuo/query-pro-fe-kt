package cn.cloudself.query.structure_reolsver

import cn.cloudself.query.QueryStructure
import cn.cloudself.query.WhereClause
import cn.cloudself.query.WhereClauseCommands
import cn.cloudself.query.exception.UnSupportException
import freemarker.template.ObjectWrapper
import java.lang.StringBuilder

object QueryStructureToSql {
    fun toSqlWithIndexedParams(qs: QueryStructure) {
        val indexedParams = mutableListOf<Any?>()

        val sql = StringBuilder()
        sql.append(qs.action.name, ' ')
//        qs.fields
        sql.append('*')
        sql.append(" FROM ")
        sql.append(qs.from.main)
        for (join in qs.from.joins) {

        }
        val wheres = qs.where
        if (wheres.isNotEmpty()) {
            fun parseWhereClause(whereClause: WhereClause) {
                val upper = whereClause.commands == WhereClauseCommands.UPPER_CASE
                if (upper) {
                    sql.append("UPPER(")
                }
                val field = whereClause.field
                if (field != null) {
                    if (field.table != null) {
                        sql.append(field.table, '.')
                    }
                    sql.append(field.column)
                }
                if (upper) {
                    sql.append(')')
                }
                val operator = whereClause.operator
                sql.append(' ', operator, ' ')
                val value = whereClause.value
                fun parseValue(v: Any?) {
                    if (v != null) {
                        sql.append(if (upper) "UPPER(?)" else '?')
                        indexedParams.add(v)
                    }
                }
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
                            parseValue(v)
                        }
                        if (vi != lastIndexOfValues) {
                            sql.append(when (operator) {
                                "in" -> ", "
                                "not in" -> ", "
                                "between" -> " AND "
                                "not between" -> " AND "
                                "or" -> " AND "
                                else -> throw UnSupportException("未知的运算符{0}", operator)
                            })
                        }
                    }
                    sql.append(')')
                } else {
                    parseValue(value)
                }
            }

            sql.append(" WHERE ")
            val lastIndexOfWheres = wheres.size - 1
            for ((i, whereClause) in wheres.withIndex()) {
                parseWhereClause(whereClause)

                if (lastIndexOfWheres != i && whereClause.operator != "or" && wheres[i + 1].operator != "or") {
                    sql.append(" AND ")
                }
            }
        }

//        qs.limit
//        qs.orderBy

        println(qs)
        println(indexedParams)
        println(sql.toString())
    }
}
