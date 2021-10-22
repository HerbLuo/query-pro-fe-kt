@file:Suppress("unused")

package cn.cloudself.helpers.query

import javax.persistence.*
import cn.cloudself.query.*

/**
 * 
 */
@Entity
@Table(name = "user")
data class User(
    /**  */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
    /**  */
    @Column(name = "name")
    var name: String? = null,
    /**  */
    @Column(name = "age")
    var age: Int? = null,
)

class ImplUserQueryPro {
    companion object {
        const val TABLE_NAME = "user"
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

        private fun createWhereField(column: String, objs: Array<out Any>) =
            createWhereField(column).let { if (objs.size == 1) it.equalsTo(objs[0]) else it.`in`(*objs) }

        val id = createWhereField("id")
        fun id(vararg ids: Any) = createWhereField("id", ids)
        val name = createWhereField("name")
        fun name(vararg names: Any) = createWhereField("name", names)
        val age = createWhereField("age")
        fun age(vararg ages: Any) = createWhereField("age", ages)
    }

    class OrderByField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        private fun createOrderByField(column: String) =
            QueryOrderByKeywords(createField(column), queryStructure, create_order_by_field)

        fun id() = createOrderByField("id")
        fun name() = createOrderByField("name")
        fun age() = createOrderByField("age")
    }

    class ColumnLimiterField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        fun id() = getColumn(createField("id"), Long::class.java)
        fun name() = getColumn(createField("name"), String::class.java)
        fun age() = getColumn(createField("age"), Int::class.java)
    }

    class ColumnsLimiterField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        private fun createColumnsLimiterField(column: String) =
            ColumnsLimiterField<T, RUN_RES>(queryStructure.copy(fields = queryStructure.fields + createField(column)), field_clazz)

        fun id() = createColumnsLimiterField("id")
        fun name() = createColumnsLimiterField("name")
        fun age() = createColumnsLimiterField("age")
    }

    class UpdateSetField(private val queryStructure: QueryStructure): UpdateField<WhereField<Boolean, Boolean>>(queryStructure, { qs: QueryStructure -> WhereField(qs, Boolean::class.java) }) {
        private fun createUpdateSetField(key: String, value: Any) = this.also {
            @Suppress("UNCHECKED_CAST") val map = queryStructure.update?.data as MutableMap<String, Any>
            map[key] = value
        }

        fun id(id: Any) = createUpdateSetField("id", id)
        fun name(name: Any) = createUpdateSetField("name", name)
        fun age(age: Any) = createUpdateSetField("age", age)
    }


    class FieldsGenerator: FieldGenerator() {
        override val tableName = TABLE_NAME

        fun id() = this.also { fields.add(createField("id")) }
        fun name() = this.also { fields.add(createField("name")) }
        fun age() = this.also { fields.add(createField("age")) }
    }
}

private fun createQuery(queryStructure: QueryStructure) =
    QueryPro<
            User,
            Long,
            ImplUserQueryPro.WhereField<User, List<User>>,
            ImplUserQueryPro.OrderByField<User, List<User>>,
            ImplUserQueryPro.UpdateSetField,
            ImplUserQueryPro.WhereField<Boolean, Boolean>,
            ImplUserQueryPro.WhereField<Boolean, Boolean>,
    > (
        User::class.java,
        queryStructure,
        { qs: QueryStructure -> ImplUserQueryPro.WhereField(qs, User::class.java) },
        { qs: QueryStructure -> ImplUserQueryPro.OrderByField(qs, User::class.java) },
        { qs: QueryStructure -> ImplUserQueryPro.UpdateSetField(qs) },
        { qs: QueryStructure -> ImplUserQueryPro.WhereField(qs, Boolean::class.java) },
        { qs: QueryStructure -> ImplUserQueryPro.WhereField(qs, Boolean::class.java) },
    )

val UserQueryPro = createQuery(QueryStructure(from = QueryStructureFrom(ImplUserQueryPro.TABLE_NAME)))

val UserQueryProEx = QueryProEx(
    QueryStructure(from = QueryStructureFrom(ImplUserQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplUserQueryPro.WhereField<User, List<User>>(qs, User::class.java) },
    { ImplUserQueryPro.FieldsGenerator() },
    { qs -> createQuery(qs) }
)
