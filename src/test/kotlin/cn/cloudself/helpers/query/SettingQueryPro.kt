@file:Suppress("unused")

package cn.cloudself.helpers.query

import javax.persistence.*
import cn.cloudself.query.*
import org.jetbrains.annotations.Contract;

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
    /**  */
    @Column(name = "deleted")
    var deleted: Boolean? = null,
)

class ImplSettingQueryPro {
    companion object {
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

        override fun getPayload() = SettingQueryPro.payload
    }

    class WhereField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.WHERE_FIELD

        private fun createWhereField(column: String) =
            QueryKeywords(createField(column), queryStructure, create_where_field)

        private fun createWhereField(column: String, objs: Array<out Any>) =
            createWhereField(column).let { if (objs.size == 1) it.equalsTo(objs[0]) else it.`in`(*objs) }

        val id = createWhereField("id")
        @Contract(pure = true)
        fun id(idList: List<Long>) = createWhereField("id", idList.toTypedArray())
        @Contract(pure = true)
        fun id(vararg ids: Long) = createWhereField("id", ids.toTypedArray())
        val userId = createWhereField("user_id")
        @Contract(pure = true)
        fun userId(userIdList: List<Long>) = createWhereField("user_id", userIdList.toTypedArray())
        @Contract(pure = true)
        fun userId(vararg userIds: Long) = createWhereField("user_id", userIds.toTypedArray())
        val kee = createWhereField("kee")
        @Contract(pure = true)
        fun kee(keeList: List<String>) = createWhereField("kee", keeList.toTypedArray())
        @Contract(pure = true)
        fun kee(vararg kees: String) = createWhereField("kee", kees)
        val value = createWhereField("value")
        @Contract(pure = true)
        fun value(valueList: List<String>) = createWhereField("value", valueList.toTypedArray())
        @Contract(pure = true)
        fun value(vararg values: String) = createWhereField("value", values)
        val deleted = createWhereField("deleted")
        @Contract(pure = true)
        fun deleted(deletedList: List<Boolean>) = createWhereField("deleted", deletedList.toTypedArray())
        @Contract(pure = true)
        fun deleted(vararg deleteds: Boolean) = createWhereField("deleted", deleteds.toTypedArray())
    }

    class OrderByField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        private fun createOrderByField(column: String) =
            QueryOrderByKeywords(createField(column), queryStructure, create_order_by_field)

        @Contract(pure = true)
        fun id() = createOrderByField("id")
        @Contract(pure = true)
        fun userId() = createOrderByField("user_id")
        @Contract(pure = true)
        fun kee() = createOrderByField("kee")
        @Contract(pure = true)
        fun value() = createOrderByField("value")
        @Contract(pure = true)
        fun deleted() = createOrderByField("deleted")
    }

    class ColumnLimiterField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        fun id() = getColumn(createField("id"), Long::class.java)
        fun userId() = getColumn(createField("user_id"), Long::class.java)
        fun kee() = getColumn(createField("kee"), String::class.java)
        fun value() = getColumn(createField("value"), String::class.java)
        fun deleted() = getColumn(createField("deleted"), Boolean::class.java)
    }

    class ColumnsLimiterField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        private fun createColumnsLimiterField(column: String) =
            ColumnsLimiterField<T, RUN_RES>(queryStructure.copy(fields = queryStructure.fields + createField(column)), field_clazz)

        @Contract(pure = true)
        fun id() = createColumnsLimiterField("id")
        @Contract(pure = true)
        fun userId() = createColumnsLimiterField("user_id")
        @Contract(pure = true)
        fun kee() = createColumnsLimiterField("kee")
        @Contract(pure = true)
        fun value() = createColumnsLimiterField("value")
        @Contract(pure = true)
        fun deleted() = createColumnsLimiterField("deleted")
    }

    class UpdateSetField(private val queryStructure: QueryStructure): UpdateField<WhereField<Boolean, Boolean>>(queryStructure, { qs: QueryStructure -> WhereField(qs, Boolean::class.java) }) {
        private fun createUpdateSetField(key: String, value: Any) = this.also {
            @Suppress("UNCHECKED_CAST") val map = queryStructure.update?.data as MutableMap<String, Any>
            map[key] = value
        }

        @Contract(pure = true)
        fun id(id: Any) = createUpdateSetField("id", id)
        @Contract(pure = true)
        fun userId(userId: Any) = createUpdateSetField("user_id", userId)
        @Contract(pure = true)
        fun kee(kee: Any) = createUpdateSetField("kee", kee)
        @Contract(pure = true)
        fun value(value: Any) = createUpdateSetField("value", value)
        @Contract(pure = true)
        fun deleted(deleted: Any) = createUpdateSetField("deleted", deleted)
    }


    class FieldsGenerator: FieldGenerator() {
        override val tableName = TABLE_NAME

        fun id() = this.also { fields.add(createField("id")) }
        fun userId() = this.also { fields.add(createField("user_id")) }
        fun kee() = this.also { fields.add(createField("kee")) }
        fun value() = this.also { fields.add(createField("value")) }
        fun deleted() = this.also { fields.add(createField("deleted")) }
    }
}

private fun createQuery(queryStructure: QueryStructure) =
    QueryPro<
            Setting,
            Long,
            ImplSettingQueryPro.WhereField<Setting, List<Setting>>,
            ImplSettingQueryPro.OrderByField<Setting, List<Setting>>,
            ImplSettingQueryPro.UpdateSetField,
            ImplSettingQueryPro.WhereField<Boolean, Boolean>,
            ImplSettingQueryPro.WhereField<Boolean, Boolean>,
    > (
        Setting::class.java,
        queryStructure,
        { qs: QueryStructure -> ImplSettingQueryPro.WhereField(qs, Setting::class.java) },
        { qs: QueryStructure -> ImplSettingQueryPro.OrderByField(qs, Setting::class.java) },
        { qs: QueryStructure -> ImplSettingQueryPro.UpdateSetField(qs) },
        { qs: QueryStructure -> ImplSettingQueryPro.WhereField(qs, Boolean::class.java) },
        { qs: QueryStructure -> ImplSettingQueryPro.WhereField(qs, Boolean::class.java) },
    )

val SettingQueryPro = createQuery(QueryStructure(from = QueryStructureFrom(ImplSettingQueryPro.TABLE_NAME)))

val SettingQueryProEx = QueryProEx(
    QueryStructure(from = QueryStructureFrom(ImplSettingQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplSettingQueryPro.WhereField<Setting, List<Setting>>(qs, Setting::class.java) },
    { ImplSettingQueryPro.FieldsGenerator() },
    { qs -> createQuery(qs) }
)
