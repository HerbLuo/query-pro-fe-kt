package cn.cloudself.query

import java.util.Date
import javax.persistence.*

/**
 * 词汇
 */
@Entity
@Table(name = "word")
data class Word(
    /**  */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
    /** 单词 */
    @Column(name = "word")
    var word: String? = null,
    /** 词频 */
    @Column(name = "score")
    var score: Double? = null,
    /** 是否删除 */
    @Column(name = "deleted")
    var deleted: Boolean? = null,
    /** 创建人 */
    @Column(name = "create_by")
    var createBy: String? = null,
    /** 创建日期 */
    @Column(name = "create_time")
    var createTime: Date? = null,
    /** 更新人 */
    @Column(name = "update_by")
    var updateBy: String? = null,
    /** 更新时间 */
    @Column(name = "update_time")
    var updateTime: Date? = null,
)

class ImplWordQueryPro {
    companion object {
        const val TABLE_NAME = "word"
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
        val word = createWhereField("word")
        val score = createWhereField("score")
        val deleted = createWhereField("deleted")
        val createBy = createWhereField("create_by")
        val createTime = createWhereField("create_time")
        val updateBy = createWhereField("update_by")
        val updateTime = createWhereField("update_time")
    }

    class OrderByField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        private fun createOrderByField(column: String) =
            QueryOrderByKeywords(createField(column), queryStructure, create_order_by_field)

        fun id() = createOrderByField("id")
        fun word() = createOrderByField("word")
        fun score() = createOrderByField("score")
        fun deleted() = createOrderByField("deleted")
        fun createBy() = createOrderByField("create_by")
        fun createTime() = createOrderByField("create_time")
        fun updateBy() = createOrderByField("update_by")
        fun updateTime() = createOrderByField("update_time")
    }

    class ColumnLimiterField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        fun id() = getColumn(createField("id"), Long::class.java)
        fun word() = getColumn(createField("word"), String::class.java)
        fun score() = getColumn(createField("score"), Double::class.java)
        fun deleted() = getColumn(createField("deleted"), Boolean::class.java)
        fun createBy() = getColumn(createField("create_by"), String::class.java)
        fun createTime() = getColumn(createField("create_time"), Date::class.java)
        fun updateBy() = getColumn(createField("update_by"), String::class.java)
        fun updateTime() = getColumn(createField("update_time"), Date::class.java)
    }

    class ColumnsLimiterField<T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class<T>): CommonField<T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        private fun createColumnsLimiterField(column: String) =
            ColumnsLimiterField<T, RUN_RES>(queryStructure.copy(fields = queryStructure.fields + createField(column)), field_clazz)

        fun id() = createColumnsLimiterField("id")
        fun word() = createColumnsLimiterField("word")
        fun score() = createColumnsLimiterField("score")
        fun deleted() = createColumnsLimiterField("deleted")
        fun createBy() = createColumnsLimiterField("create_by")
        fun createTime() = createColumnsLimiterField("create_time")
        fun updateBy() = createColumnsLimiterField("update_by")
        fun updateTime() = createColumnsLimiterField("update_time")
    }

    class FieldsGenerator: FieldGenerator() {
        override val tableName = TABLE_NAME

        fun id() = this.also { fields.add(createField("id")) }
        fun word() = this.also { fields.add(createField("word")) }
        fun score() = this.also { fields.add(createField("score")) }
        fun deleted() = this.also { fields.add(createField("deleted")) }
        fun createBy() = this.also { fields.add(createField("create_by")) }
        fun createTime() = this.also { fields.add(createField("create_time")) }
        fun updateBy() = this.also { fields.add(createField("update_by")) }
        fun updateTime() = this.also { fields.add(createField("update_time")) }
    }
}

private fun createQuery(queryStructure: QueryStructure) =
    QueryPro(
        queryStructure,
        { qs: QueryStructure -> ImplWordQueryPro.WhereField<Word, List<Word>>(qs, Word::class.java) },
        { qs: QueryStructure -> ImplWordQueryPro.OrderByField<Word, List<Word>>(qs, Word::class.java) },
        { qs: QueryStructure -> ImplWordQueryPro.WhereField<Boolean, Boolean>(qs, Boolean::class.java) },
        { qs: QueryStructure -> ImplWordQueryPro.WhereField<Boolean, Boolean>(qs, Boolean::class.java) },
    )

val WordQueryPro = createQuery(QueryStructure(from = QueryStructureFrom(ImplWordQueryPro.TABLE_NAME)));

val WordQueryProEx = QueryProEx(
    QueryStructure(from = QueryStructureFrom(ImplWordQueryPro.TABLE_NAME)),
    { qs: QueryStructure -> ImplWordQueryPro.WhereField<Word, List<Word>>(qs, Word::class.java) },
    { ImplWordQueryPro.FieldsGenerator() },
    { qs -> createQuery(qs) }
)


