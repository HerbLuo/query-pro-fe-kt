package cn.cloudself.query

data class QueryStructure(
    var action: QueryStructureAction = QueryStructureAction.SELECT,
    var update: Update? = null,
    var fields: List<Field> = listOf(),
    var from: QueryStructureFrom = QueryStructureFrom(),
    var where: List<WhereClause> = listOf(),
    var orderBy: List<OrderByClause> = listOf(),
    var limit: Pair<Int, Int>? = null,
)

data class Update(
    var data: Any? = null,
    var override: Boolean = false,
    var id: String? = null,
)

data class OrderByClause(
    var field: Field,
    var operator: String,
)

data class Field(
    var table: String? = null,
    var column: String,
    var commands: FieldCommands? = null,
)

enum class FieldCommands {
    UPPER_CASE,
}

enum class WhereClauseCommands {
    UPPER_CASE,
}

data class WhereClause(
    var field: Field? = null,
    var operator: String,
    var value: Any? = null, // null arrayOr<string boolean integer long date> List<WhereClause>
    var commands: WhereClauseCommands? = null,
    var sql: String? = null,
)

enum class QueryStructureAction {
    SELECT,
    UPDATE,
    DELETE,
    INSERT,
}

data class FromJoinerOn(
    var left: Field,
    var right: Field,
)

enum class JoinType {
    LEFT_JOIN,
}

data class FromJoiner(
    var type: JoinType,
    var table: String,
    var on: List<FromJoinerOn>,
)

data class QueryStructureFrom(
    var main: String = "?",
    var joins: List<FromJoiner> = listOf(),
)

/**
 * 支持JavaBean, 支持Map<String, *>
 */
typealias SupportedInsertClazz = Class<*>

/**
 * 支持JavaBean, 支持Map<String, *> 支持基本类型(Long, String, Date, Enum等, 具体参考[QueryProConfig.global.addResultSetParser]),
 */
typealias SupportedQueryClazz = Class<*>

/**
 * 支持的值有：Int::class.java 总共影响的条数
 *           IntArray::class.java, List::class.java, listOf<Int>().javaClass等 代表每条语句更新的条数
 *           Boolean::class.java 不建议使用，除非你知道自己在做什么，仅简单的判断了总更新条数是否大于1
 */
typealias SupportedUpdatedBatchClazz = Class<*>
