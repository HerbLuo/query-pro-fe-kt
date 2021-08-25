package cn.cloudself.query

typealias CreateFieldGenerator<FIELD_GENERATOR> = () -> FIELD_GENERATOR

abstract class FieldGenerator {
    val fields: MutableList<Field> = mutableListOf()
    abstract val tableName: String
}

class QueryProEx<
        QUERY,
        WHERE_FIELD: QueryField<*, *, *, *, *>,
        FIELD_GENERATOR: FieldGenerator,
> constructor(
    private val queryStructure: QueryStructure,
    private val createWhereField: CreateQueryField<WHERE_FIELD>,
    private val createFieldGenerator: CreateFieldGenerator<FIELD_GENERATOR>,
    private val createQuery: CreateQuery<QUERY>
) {
    fun leftJoinOn(
        fields1: FieldGenerator,
        fields2: FieldGenerator
    ): QUERY {
        val oldFrom = queryStructure.from
        val oldJoins = oldFrom.joins
        val currentTableName = queryStructure.from.main
        val field1TableName = fields1.tableName
        val field2TableName = fields2.tableName

        var foreignTableName: String? = null
        var foreignFields: List<Field>? = null
        var currentTableFields: List<Field>? = null

        if (field1TableName == currentTableName) { // 其中一个是主表
            foreignTableName = field2TableName
            foreignFields = fields2.fields
            currentTableFields = fields1.fields
        } else if (field2TableName == currentTableName) { // 其中一个是主表
            foreignTableName = field1TableName
            foreignFields = fields1.fields
            currentTableFields = fields2.fields
        } else { // 没有主表，从joins里面找已有表
            for (oldJoin in oldJoins) {
                if (oldJoin.table == field1TableName) {
                    foreignTableName = field2TableName
                    foreignFields = fields2.fields
                    currentTableFields = fields1.fields
                    break
                }
                if (oldJoin.table == field2TableName) {
                    foreignTableName = field1TableName
                    foreignFields = fields1.fields
                    currentTableFields = fields2.fields
                    break
                }
            }
        }
        if (foreignFields == null || currentTableFields == null || foreignTableName == null) {
            throw RuntimeException("can not find left table for joiner1 and joiner2: $field1TableName, $field2TableName")
        }
        if (foreignFields.size != currentTableFields.size) {
            throw RuntimeException("the joiner length of $currentTableFields and $foreignFields is not equal")
        }

        val foreignJoinerOn = foreignFields.mapIndexed { index, field -> FromJoinerOn(currentTableFields[index], field) }
        val newJoins = oldJoins + FromJoiner(JoinType.LEFT_JOIN, foreignTableName, foreignJoinerOn)

        return createQuery(queryStructure.copy(from = oldFrom.copy(joins = newJoins)))
    }

    fun joiner() = createFieldGenerator()

    fun foreignField() = createWhereField(queryStructure)
}
