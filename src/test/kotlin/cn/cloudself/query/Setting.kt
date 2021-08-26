package cn.cloudself.query

data class Setting(
    var id: Long?,
    var userId: Long?,
    var kee: String?,
    var value: String?,
)

class ImplSettingQueryPro {
    companion object {
        val CLAZZ = Setting::class.java
        const val TABLE_NAME = "setting"
        private fun createField(column: String) = Field(TABLE_NAME, column)
    }

    abstract class CommonField constructor(queryStructure: QueryStructure)
        : QueryField<Setting, WhereField, OrderByField, ColumnLimiterField, ColumnsLimiterField>(queryStructure) {
        override val field_clazz = CLAZZ
        override val create_where_field: CreateQueryField<WhereField> = { queryStructure -> WhereField(queryStructure) }
        override val create_order_by_field: CreateQueryField<OrderByField> = { queryStructure -> OrderByField(queryStructure) }
        override val create_column_limiter_field: CreateQueryField<ColumnLimiterField> =
            { queryStructure -> ColumnLimiterField(queryStructure) }
        override val create_columns_limiter_field: CreateQueryField<ColumnsLimiterField> =
            { queryStructure -> ColumnsLimiterField(queryStructure) }
    }

    class WhereField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.WHERE_FIELD

        private fun createWhereField(column: String) =
            QueryKeywords(createField(column), queryStructure, create_where_field)

        val id = createWhereField("id")
        val userId = createWhereField("userId")
        val kee = createWhereField("kee")
        val value = createWhereField("value")
    }

    class OrderByField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        private fun createOrderByField(column: String) =
            QueryOrderByKeywords(createField(column), queryStructure, create_order_by_field)

        fun id() = createOrderByField("id")
        fun userId() = createOrderByField("userId")
        fun kee() = createOrderByField("kee")
        fun value() = createOrderByField("value")
    }

    class ColumnLimiterField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.OTHER_FIELD

        fun id() = getColumn(createField("id"), Long::class.java)
        fun userId() = getColumn(createField("userId"), Long::class.java)
        fun kee() = getColumn(createField("kee"), String::class.java)
        fun value() = getColumn(createField("value"), String::class.java)
    }

    class ColumnsLimiterField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.OTHER_FIELD

        private fun createColumnsLimiterField(column: String) =
            ColumnsLimiterField(queryStructure.copy(fields = queryStructure.fields + createField(column)))

        fun id() = createColumnsLimiterField("id")
        fun userId() = createColumnsLimiterField("userId")
        fun kee() = createColumnsLimiterField("kee")
        fun value() = createColumnsLimiterField("value")
    }

    class FieldsGenerator: FieldGenerator() {
        override val tableName = TABLE_NAME

        fun id() = this.also { fields.add(createField("id")) }
        fun userId() = this.also { fields.add(createField("userId")) }
        fun kee() = this.also { fields.add(createField("kee")) }
        fun value() = this.also { fields.add(createField("value")) }
    }
}

val SettingQueryPro = QueryPro(
    QueryStructure(from = QueryStructureFrom(ImplSettingQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplSettingQueryPro.WhereField(qs) },
    { qs: QueryStructure -> ImplSettingQueryPro.OrderByField(qs) }
)

val SettingQueryProEx = QueryProEx(
    QueryStructure(from = QueryStructureFrom(ImplSettingQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplSettingQueryPro.WhereField(qs) },
    { ImplSettingQueryPro.FieldsGenerator() },
    { SettingQueryPro }
)