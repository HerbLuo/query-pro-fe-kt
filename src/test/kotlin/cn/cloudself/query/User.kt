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
        override val field_clazz = User::class.java
        override val create_where_field: CreateQueryField<WhereField> = { queryStructure -> WhereField(queryStructure) }
        override val create_order_by_field: CreateQueryField<OrderByField> = { queryStructure -> OrderByField(queryStructure) }
        override val create_column_limiter_field: CreateQueryField<ColumnLimiterField> =
            { queryStructure -> ColumnLimiterField(queryStructure) }
        override val create_columns_limiter_field: CreateQueryField<ColumnsLimiterField> =
            { queryStructure -> ColumnsLimiterField(queryStructure) }
    }

    class WhereField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.WHERE_FIELD

        val id = QueryKeywords(createField("id"), queryStructure, create_where_field)
        val name = QueryKeywords(createField("name"), queryStructure, create_where_field)
        val age = QueryKeywords(createField("age"), queryStructure, create_where_field)
    }

    class OrderByField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        fun id() = QueryOrderByKeywords(createField("id"), queryStructure, create_order_by_field)
        fun name() = QueryOrderByKeywords(createField("name"), queryStructure, create_order_by_field)
        fun age() = QueryOrderByKeywords(createField("age"), queryStructure, create_order_by_field)
    }

    class ColumnLimiterField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.OTHER_FIELD

        fun id() = getColumn(createField("id"), Long::class.java)
        fun name() = getColumn(createField("name"), String::class.java)
        fun age() = getColumn(createField("age"), Int::class.java)
    }

    class ColumnsLimiterField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.OTHER_FIELD

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

private fun createQuery(queryStructure: QueryStructure) =
    QueryPro(
        queryStructure,
        { qs: QueryStructure -> ImplUserQueryPro.WhereField(qs) },
        { qs: QueryStructure -> ImplUserQueryPro.OrderByField(qs) }
    )

val UserQueryPro = createQuery(QueryStructure(from = QueryStructureFrom(ImplUserQueryPro.TABLE_NAME)))

val UserQueryProEx = QueryProEx(
    QueryStructure(from = QueryStructureFrom(ImplUserQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplUserQueryPro.WhereField(qs) },
    { ImplUserQueryPro.FieldsGenerator() },
    { qs -> createQuery(qs) }
)
