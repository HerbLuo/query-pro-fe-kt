package cn.cloudself.query

data class QueryStructure(
    val action: QueryStructureAction = QueryStructureAction.SELECT,
    val update: Update? = null,
    val fields: List<Field> = listOf(),
    var from: QueryStructureFrom = QueryStructureFrom(),
    val where: List<WhereClause> = listOf(),
    val orderBy: List<OrderByClause> = listOf(),
    val limit: Pair<Int, Int>? = null,
)

data class Update(
    val data: Any? = null,
    val override: Boolean = false,
    val id: String? = null,
)

data class OrderByClause(
    val field: Field,
    val operator: String,
)

data class Field(
    val table: String? = null,
    val column: String,
    val commands: FieldCommands? = null,
)

enum class FieldCommands {
    UPPER_CASE,
}

enum class WhereClauseCommands {
    UPPER_CASE,
}

data class WhereClause(
    val field: Field? = null,
    val operator: String,
    val value: Any? = null, // null arrayOr<string boolean integer long date> List<WhereClause>
    val commands: WhereClauseCommands? = null,
    val sql: String? = null,
)

enum class QueryStructureAction {
    SELECT,
    UPDATE,
    DELETE,
    INSERT,
}

data class FromJoinerOn(
    val left: Field,
    val right: Field,
)

enum class JoinType {
    LEFT_JOIN,
}

data class FromJoiner(
    val type: JoinType,
    val table: String,
    val on: List<FromJoinerOn>,
)

data class QueryStructureFrom(
    val main: String = "?",
    val joins: List<FromJoiner> = listOf(),
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
