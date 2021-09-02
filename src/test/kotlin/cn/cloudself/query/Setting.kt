package cn.cloudself.query

import javax.persistence.*
import cn.cloudself.query.*

/**
 *
 */
@Entity
@Table(name = "setting")
data class Setting(
    /**  */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
    /**  */
    @Column(name = "user_id")
    var userId: Long? = null,
    /**  */
    @Column(name = "kee")
    var kee: String? = null,
    /**  */
    @Column(name = "value")
    var value: String? = null,
)

class ImplSettingQueryPro {
    companion object {
        val CLAZZ = Setting::class.java
        const val TABLE_NAME = "setting"
        private fun createField(column: String) = Field(TABLE_NAME, column)
    }

    abstract class CommonField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>)
        : QueryField<T, RUN_RES, WhereField<T, RUN_RES>, OrderByField<T, RUN_RES>, ColumnLimiterField<T, RUN_RES>, ColumnsLimiterField<T, RUN_RES>>(queryStructure, field_clazz) {
        override val create_where_field: CreateQueryField<WhereField<T, RUN_RES>> = { queryStructure -> WhereField(queryStructure, field_clazz) }
        override val create_order_by_field: CreateQueryField<OrderByField<T, RUN_RES>> = { queryStructure -> OrderByField(queryStructure, field_clazz) }
        override val create_column_limiter_field: CreateQueryField<ColumnLimiterField<T, RUN_RES>> =
            { queryStructure -> ColumnLimiterField(queryStructure, field_clazz) }
        override val create_columns_limiter_field: CreateQueryField<ColumnsLimiterField<T, RUN_RES>> =
            { queryStructure -> ColumnsLimiterField(queryStructure, field_clazz) }
    }

    class WhereField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.WHERE_FIELD

        private fun createWhereField(column: String) =
            QueryKeywords(createField(column), queryStructure, create_where_field)

        val id = createWhereField("id")
        val userId = createWhereField("user_id")
        val kee = createWhereField("kee")
        val value = createWhereField("value")
    }

    class OrderByField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        private fun createOrderByField(column: String) =
            QueryOrderByKeywords(createField(column), queryStructure, create_order_by_field)

        fun id() = createOrderByField("id")
        fun userId() = createOrderByField("user_id")
        fun kee() = createOrderByField("kee")
        fun value() = createOrderByField("value")
    }

    class ColumnLimiterField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        fun id() = getColumn(createField("id"), Long::class.java)
        fun userId() = getColumn(createField("user_id"), Long::class.java)
        fun kee() = getColumn(createField("kee"), String::class.java)
        fun value() = getColumn(createField("value"), String::class.java)
    }

    class ColumnsLimiterField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        private fun createColumnsLimiterField(column: String) =
            ColumnsLimiterField<T, RUN_RES>(queryStructure.copy(fields = queryStructure.fields + createField(column)), field_clazz)

        fun id() = createColumnsLimiterField("id")
        fun userId() = createColumnsLimiterField("user_id")
        fun kee() = createColumnsLimiterField("kee")
        fun value() = createColumnsLimiterField("value")
    }

    class FieldsGenerator: FieldGenerator() {
        override val tableName = TABLE_NAME

        fun id() = this.also { fields.add(createField("id")) }
        fun userId() = this.also { fields.add(createField("user_id")) }
        fun kee() = this.also { fields.add(createField("kee")) }
        fun value() = this.also { fields.add(createField("value")) }
    }
}

private fun createQuery(queryStructure: QueryStructure) =
    QueryPro(
        queryStructure,
        { qs: QueryStructure -> ImplSettingQueryPro.WhereField<Setting, List<Setting>>(qs, Setting::class.java) },
        { qs: QueryStructure -> ImplSettingQueryPro.OrderByField<Setting, List<Setting>>(qs, Setting::class.java) },
        { qs: QueryStructure -> ImplSettingQueryPro.WhereField<Boolean, Boolean>(qs, Boolean::class.java) },
        { qs: QueryStructure -> ImplSettingQueryPro.WhereField<Boolean, Boolean>(qs, Boolean::class.java) },
    )

val SettingQueryPro = createQuery(QueryStructure(from = QueryStructureFrom(ImplSettingQueryPro.TABLE_NAME)))

val SettingQueryProEx = QueryProEx(
    QueryStructure(from = QueryStructureFrom(ImplSettingQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplSettingQueryPro.WhereField<Setting, List<Setting>>(qs, Setting::class.java) },
    { ImplSettingQueryPro.FieldsGenerator() },
    { qs -> createQuery(qs) }
)
