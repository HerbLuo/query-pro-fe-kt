<#-- @ftlvariable name="m" type="cn.cloudself.query.util.TemplateModel" -->
<#assign ClassName = m._ClassName/>
<#assign EntityName = m._EntityName/>
package ${m.packagePath}

import ${m.entityPackage}.${EntityName}
<#if m.hasBigDecimal>import java.math.BigDecimal
</#if><#if m.hasDate>import java.util.Date
</#if>import cn.cloudself.query.*

class Impl${ClassName} {
    companion object {
        val CLAZZ = ${EntityName}::class.java
        const val TABLE_NAME = "${m.db_name}"
        private fun createField(column: String) = Field(TABLE_NAME, column)
    }

    abstract class CommonField constructor(queryStructure: QueryStructure)
        : QueryField${"<"}${EntityName}, WhereField, OrderByField, ColumnLimiterField, ColumnsLimiterField>(queryStructure) {
        override val field_clazz = CLAZZ
        override val create_where_field: CreateQueryField${"<"}WhereField> = { queryStructure -> WhereField(queryStructure) }
        override val create_order_by_field: CreateQueryField${"<"}OrderByField> = { queryStructure -> OrderByField(queryStructure) }
        override val create_column_limiter_field: CreateQueryField${"<"}ColumnLimiterField> =
            { queryStructure -> ColumnLimiterField(queryStructure) }
        override val create_columns_limiter_field: CreateQueryField${"<"}ColumnsLimiterField> =
            { queryStructure -> ColumnsLimiterField(queryStructure) }
    }

    class WhereField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.WHERE_FIELD

        private fun createWhereField(column: String) =
            QueryKeywords(createField(column), queryStructure, create_where_field)

    <#list m.columns as field>
        val ${field.propertyName} = createWhereField("${field.db_name}")
    </#list>
    }

    class OrderByField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        private fun createOrderByField(column: String) =
            QueryOrderByKeywords(createField(column), queryStructure, create_order_by_field)

    <#list m.columns as field>
        fun ${field.propertyName}() = createOrderByField("${field.db_name}")
    </#list>
    }

    class ColumnLimiterField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.OTHER_FIELD

    <#list m.columns as field>
        fun ${field.propertyName}() = getColumn(createField("${field.db_name}"), ${field.ktTypeStr}::class.java)
    </#list>
    }

    class ColumnsLimiterField constructor(queryStructure: QueryStructure): CommonField(queryStructure) {
        override val field_type = QueryFieldType.OTHER_FIELD

        private fun createColumnsLimiterField(column: String) =
            ColumnsLimiterField(queryStructure.copy(fields = queryStructure.fields + createField(column)))

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

val ${ClassName} = QueryPro(
    QueryStructure(from = QueryStructureFrom(Impl${ClassName}.TABLE_NAME)),
    { qs: QueryStructure -> Impl${ClassName}.WhereField(qs) },
    { qs: QueryStructure -> Impl${ClassName}.OrderByField(qs) }
)

val ${ClassName}Ex = QueryProEx(
    QueryStructure(from = QueryStructureFrom(Impl${ClassName}.TABLE_NAME)),
    { qs: QueryStructure -> Impl${ClassName}.WhereField(qs) },
    { Impl${ClassName}.FieldsGenerator() },
    { ${ClassName} }
)
