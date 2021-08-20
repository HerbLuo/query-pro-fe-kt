package cn.cloudself.query

data class User(
    private val id: Long,
    private val name: String,
    private val age: Int,
)

class ImplUserQueryPro {
    companion object {
        const val TABLE_NAME = "user"
        private fun createField(column: String) = Field(TABLE_NAME, column)
    }

    abstract class CommonField constructor(queryStructure: QueryStructure)
        : QueryField<User, WhereField, OrderByField, ColumnLimiterField, ColumnsLimiterField>(queryStructure) {
        override val clazz = User::class.java
        override val createWhereField: CreateQueryField<WhereField> = { queryStructure -> WhereField(queryStructure) }
        override val createOrderByField: CreateQueryField<OrderByField> = { queryStructure -> OrderByField(queryStructure) }
        override val createColumnLimiterField: CreateQueryField<ColumnLimiterField> =
            { queryStructure -> ColumnLimiterField(queryStructure) }
        override val createColumnsLimiterField: CreateQueryField<ColumnsLimiterField> =
            { queryStructure -> ColumnsLimiterField(queryStructure) }
    }

    class WhereField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val type = QueryFieldType.WHERE_FIELD

        val id = QueryKeywords(createField("id"), queryStructure, createWhereField)
        val name = QueryKeywords(createField("name"), queryStructure, createWhereField)
        val age = QueryKeywords(createField("age"), queryStructure, createWhereField)
    }

    class OrderByField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val type = QueryFieldType.ORDER_BY_FIELD

        fun id() = QueryOrderByKeywords(createField("id"), queryStructure, createOrderByField)
        fun name() = QueryOrderByKeywords(createField("name"), queryStructure, createOrderByField)
        fun age() = QueryOrderByKeywords(createField("age"), queryStructure, createOrderByField)
    }

    class ColumnLimiterField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val type = QueryFieldType.OTHER_FIELD

        fun id() = getColumn(createField("id"), Long::class.java)
        fun name() = getColumn(createField("name"), String::class.java)
        fun age() = getColumn(createField("age"), Int::class.java)
    }

    class ColumnsLimiterField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val type = QueryFieldType.OTHER_FIELD

        fun id() = ColumnsLimiterField(queryStructure.copy(fields = queryStructure.fields + createField("id")))
        fun name() = ColumnsLimiterField(queryStructure.copy(fields = queryStructure.fields + createField("name")))
        fun age() = ColumnsLimiterField(queryStructure.copy(fields = queryStructure.fields + createField("age")))
    }

    class FieldsGenerator: FieldGenerator() {
        override val tableName = TABLE_NAME

        fun id() = this.also { fields.add(createField("id")) }
        fun name() = this.also { fields.add(createField("name")) }
        fun age() = this.also { fields.add(createField("age")) }
    }
}

val UserQueryPro = QueryPro(
    QueryStructure(from = QueryStructureFrom(ImplUserQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplUserQueryPro.WhereField(qs) },
    { qs: QueryStructure -> ImplUserQueryPro.OrderByField(qs) }
)

val UserQueryProEx = QueryProEx(
    QueryStructure(from = QueryStructureFrom(ImplUserQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplUserQueryPro.WhereField(qs) },
    { ImplUserQueryPro.FieldsGenerator() },
    { UserQueryPro }
)
