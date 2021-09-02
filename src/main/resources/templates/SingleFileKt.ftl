<#-- @ftlvariable name="m" type="cn.cloudself.query.util.TemplateModel" -->
<#assign ClassName = m._ClassName/>
<#assign EntityName = m._EntityName/>
package ${m.packagePath}

<#if m.hasBigDecimal>import java.math.BigDecimal
</#if><#if m.hasDate>import java.util.Date
</#if>import javax.persistence.*
import cn.cloudself.query.*

<#--@Entity-->
<#--@Table(name = "attachment", schema = "rcms")-->
/**
 * ${m.remark}
 */
<#if m.id??>@Entity</#if>
@Table(name = "${m.db_name}")
data class ${EntityName}(
<#list m.columns as field>
    /** ${field.remark} */
<#if m.id?? && m.id.column == field.db_name>
    @Id<#if m.id.autoIncrement>
    @GeneratedValue(strategy = GenerationType.IDENTITY)</#if>
</#if>
    @Column(name = "${field.db_name}")
    var ${field.propertyName}: ${field.ktTypeStr}?<#if !m.noArgMode> = null</#if>,
</#list>
)

class Impl${ClassName} {
    companion object {
        val CLAZZ = ${EntityName}::class.java
        const val TABLE_NAME = "${m.db_name}"
        private fun createField(column: String) = Field(TABLE_NAME, column)
    }

    abstract class CommonField${"<"}T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class${"<"}T>)
        : QueryField${"<"}T, RUN_RES, WhereField${"<"}T, RUN_RES>, OrderByField${"<"}T, RUN_RES>, ColumnLimiterField${"<"}T, RUN_RES>, ColumnsLimiterField${"<"}T, RUN_RES>>(queryStructure, field_clazz) {
        override val create_where_field: CreateQueryField${"<"}WhereField${"<"}T, RUN_RES>> = { queryStructure -> WhereField(queryStructure, field_clazz) }
        override val create_order_by_field: CreateQueryField${"<"}OrderByField${"<"}T, RUN_RES>> = { queryStructure -> OrderByField(queryStructure, field_clazz) }
        override val create_column_limiter_field: CreateQueryField${"<"}ColumnLimiterField${"<"}T, RUN_RES>> =
            { queryStructure -> ColumnLimiterField(queryStructure, field_clazz) }
        override val create_columns_limiter_field: CreateQueryField${"<"}ColumnsLimiterField${"<"}T, RUN_RES>> =
            { queryStructure -> ColumnsLimiterField(queryStructure, field_clazz) }
    }

    class WhereField${"<"}T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class${"<"}T>): CommonField${"<"}T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.WHERE_FIELD

        private fun createWhereField(column: String) =
            QueryKeywords(createField(column), queryStructure, create_where_field)

    <#list m.columns as field>
        val ${field.propertyName} = createWhereField("${field.db_name}")
    </#list>
    }

    class OrderByField${"<"}T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class${"<"}T>): CommonField${"<"}T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        private fun createOrderByField(column: String) =
            QueryOrderByKeywords(createField(column), queryStructure, create_order_by_field)

    <#list m.columns as field>
        fun ${field.propertyName}() = createOrderByField("${field.db_name}")
    </#list>
    }

    class ColumnLimiterField${"<"}T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class${"<"}T>): CommonField${"<"}T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

    <#list m.columns as field>
        fun ${field.propertyName}() = getColumn(createField("${field.db_name}"), ${field.ktTypeStr}::class.java)
    </#list>
    }

    class ColumnsLimiterField${"<"}T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class${"<"}T>): CommonField${"<"}T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.OTHER_FIELD

        private fun createColumnsLimiterField(column: String) =
            ColumnsLimiterField${"<"}T, RUN_RES>(queryStructure.copy(fields = queryStructure.fields + createField(column)), field_clazz)

    <#list m.columns as field>
        fun ${field.propertyName}() = createColumnsLimiterField("${field.db_name}")
    </#list>
    }

    class FieldsGenerator: FieldGenerator() {
        override val tableName = TABLE_NAME

    <#list m.columns as field>
        fun ${field.propertyName}() = this.also { fields.add(createField("${field.db_name}")) }
    </#list>
    }
}

private fun createQuery(queryStructure: QueryStructure) =
    QueryPro(
        queryStructure,
        { qs: QueryStructure -> Impl${ClassName}.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>(qs, ${EntityName}::class.java) },
        { qs: QueryStructure -> Impl${ClassName}.OrderByField${"<"}${EntityName}, List${"<"}${EntityName}>>(qs, ${EntityName}::class.java) },
        { qs: QueryStructure -> Impl${ClassName}.WhereField${"<"}Boolean, Boolean>(qs, Boolean::class.java) },
        { qs: QueryStructure -> Impl${ClassName}.WhereField${"<"}Boolean, Boolean>(qs, Boolean::class.java) },
    )

val ${ClassName} = createQuery(QueryStructure(from = QueryStructureFrom(Impl${ClassName}.TABLE_NAME)))

val ${ClassName}Ex = QueryProEx(
    QueryStructure(from = QueryStructureFrom(Impl${ClassName}.TABLE_NAME)),
    { qs: QueryStructure -> Impl${ClassName}.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>(qs, ${EntityName}::class.java) },
    { Impl${ClassName}.FieldsGenerator() },
    { qs -> createQuery(qs) }
)
