<#-- @ftlvariable name="m" type="cn.cloudself.query.util.TemplateModel" -->
<#assign ClassName = m._ClassName/>
<#assign EntityName = m._EntityName/>
<#assign IdType = (m.id.javaTypeStr)!"Long"/>
@file:Suppress("unused")

package ${m.packagePath}

import ${m.entityPackage}.${EntityName}
<#if m.hasBigDecimal>import java.math.BigDecimal
</#if><#if m.hasDate>import java.util.Date
</#if>import cn.cloudself.query.*
import org.jetbrains.annotations.Contract;

class Impl${ClassName} {
    companion object {
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

        private fun createWhereField(column: String, objs: Array${"<"}out Any>) =
            createWhereField(column).let { if (objs.size == 1) it.equalsTo(objs[0]) else it.`in`(*objs) }

    <#list m.columns as field>
        val ${field.propertyName} = createWhereField("${field.db_name}")
        @Contract(pure = true)
        fun ${field.propertyName}(${field.propertyName}List: List<${field.ktTypeStr}>) = createWhereField("${field.db_name}", ${field.propertyName}List.toTypedArray())
        @Contract(pure = true)
        fun ${field.propertyName}(vararg ${field.propertyName}s: ${field.ktTypeStr}) = createWhereField("${field.db_name}", <#if field.primary>${field.propertyName}s.toTypedArray()</#if><#if !field.primary>${field.propertyName}s</#if>)
    </#list>
    }

    class OrderByField${"<"}T, RUN_RES> constructor(queryStructure: QueryStructure, field_clazz: Class${"<"}T>): CommonField${"<"}T, RUN_RES>(queryStructure, field_clazz) {
        override val field_type = QueryFieldType.ORDER_BY_FIELD

        private fun createOrderByField(column: String) =
            QueryOrderByKeywords(createField(column), queryStructure, create_order_by_field)

    <#list m.columns as field>
        @Contract(pure = true)
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
        @Contract(pure = true)
        fun ${field.propertyName}() = createColumnsLimiterField("${field.db_name}")
    </#list>
    }

    class UpdateSetField(private val queryStructure: QueryStructure): UpdateField${"<"}WhereField${"<"}Boolean, Boolean>>(queryStructure, { qs: QueryStructure -> WhereField(qs, Boolean::class.java) }) {
        private fun createUpdateSetField(key: String, value: Any) = this.also {
            @Suppress("UNCHECKED_CAST") val map = queryStructure.update?.data as MutableMap${"<"}String, Any>
            map[key] = value
        }

    <#list m.columns as field>
        <#assign prop = field.propertyName/>
        @Contract(pure = true)
        fun ${prop}(${prop}: Any) = createUpdateSetField("${field.db_name}", ${prop})
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
    QueryPro<
            ${EntityName},
            ${IdType},
            Impl${ClassName}.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            Impl${ClassName}.OrderByField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            Impl${ClassName}.UpdateSetField,
            Impl${ClassName}.WhereField${"<"}Boolean, Boolean>,
            Impl${ClassName}.WhereField${"<"}Boolean, Boolean>,
    > (
        ${EntityName}::class.java,
        queryStructure,
        { qs: QueryStructure -> Impl${ClassName}.WhereField(qs, ${EntityName}::class.java) },
        { qs: QueryStructure -> Impl${ClassName}.OrderByField(qs, ${EntityName}::class.java) },
        { qs: QueryStructure -> Impl${ClassName}.UpdateSetField(qs) },
        { qs: QueryStructure -> Impl${ClassName}.WhereField(qs, Boolean::class.java) },
        { qs: QueryStructure -> Impl${ClassName}.WhereField(qs, Boolean::class.java) },
    )

val ${ClassName} = createQuery(QueryStructure(from = QueryStructureFrom(Impl${ClassName}.TABLE_NAME)))

val ${ClassName}Ex = QueryProEx(
    QueryStructure(from = QueryStructureFrom(Impl${ClassName}.TABLE_NAME)),
    { qs: QueryStructure -> Impl${ClassName}.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>(qs, ${EntityName}::class.java) },
    { Impl${ClassName}.FieldsGenerator() },
    { qs -> createQuery(qs) }
)
