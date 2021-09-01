<#-- @ftlvariable name="m" type="cn.cloudself.query.util.TemplateModel" -->
<#assign ClassName = m._ClassName/>
<#assign EntityName = m._EntityName/>
package ${m.packagePath};

import ${m.entityPackage}.${EntityName};
<#if m.hasBigDecimal>import java.math.BigDecimal;
</#if><#if m.hasDate>import java.util.Date;
</#if>import cn.cloudself.query.*;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ${ClassName} {
    private static QueryStructure defQueryStructure() {
        final QueryStructure queryStructure = new QueryStructure();
        queryStructure.setFrom(new QueryStructureFrom(__Impl.TABLE_NAME, new ArrayList<>()));
        return queryStructure;
    }

    private static QueryPro${"<"}__Impl.WhereField, __Impl.OrderByField> createQuery(QueryStructure queryStructure) {
        return new QueryPro<>(queryStructure, __Impl.WhereField::new, __Impl.OrderByField::new);
    }

    private static final QueryPro${"<"}__Impl.WhereField, __Impl.OrderByField> queryPro = createQuery(defQueryStructure());

    public static final QueryProEx<
            QueryPro${"<"}__Impl.WhereField, __Impl.OrderByField>,
            __Impl.WhereField,
            __Impl.FieldsGenerator
    > EX = new QueryProEx<>(
            defQueryStructure(),
            __Impl.WhereField::new,
            __Impl.FieldsGenerator::new,
            ${ClassName}::createQuery
    );

    public static __Impl.WhereField selectBy() {
        return queryPro.selectBy();
    }

    public static __Impl.OrderByField orderBy() {
        return queryPro.orderBy();
    }

    public static __Impl.WhereField deleteBy() {
        return queryPro.deleteBy();
    }

    public static __Impl.WhereField selectOneBy() {
        return queryPro.selectOneBy();
    }

    public static __Impl.WhereField updateBy() {
        return queryPro.updateBy();
    }

    public static class __Impl {
        private static final Class${"<"}${EntityName}> CLAZZ = ${EntityName}.class;
        public static final String TABLE_NAME = "${m.db_name}";
        private static Field createField(String column) { return new Field(TABLE_NAME, column, null); }

        public abstract static class CommonField extends QueryField${"<"}${EntityName}, WhereField, OrderByField, ColumnLimiterField, ColumnsLimiterField> {
            public CommonField(QueryStructure queryStructure) { super(queryStructure); }

            @NotNull
            @Override
            protected Class${"<"}${EntityName}> getField_clazz() { return CLAZZ; }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, WhereField> getCreate_where_field() { return WhereField::new; }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, OrderByField> getCreate_order_by_field() { return OrderByField::new; }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, ColumnLimiterField> getCreate_column_limiter_field() { return ColumnLimiterField::new; }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, ColumnsLimiterField> getCreate_columns_limiter_field() { return ColumnsLimiterField::new; }
        }

        public static class WhereField extends CommonField {
            public WhereField(QueryStructure queryStructure) { super(queryStructure); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.WHERE_FIELD; }

            private QueryKeywords${"<"}WhereField> createWhereField(String column) {
                return new QueryKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_where_field());
            }

        <#list m.columns as field>
            public QueryKeywords${"<"}WhereField> ${field.propertyName}() { return createWhereField("${field.db_name}"); }
        </#list>
        }

        public static class OrderByField extends CommonField {
            public OrderByField(QueryStructure queryStructure) { super(queryStructure); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.ORDER_BY_FIELD; }

            private QueryOrderByKeywords${"<"}OrderByField> createOrderByField(String column) {
                return new QueryOrderByKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_order_by_field());
            }

        <#list m.columns as field>
            public QueryOrderByKeywords${"<"}OrderByField> ${field.propertyName}() { return createOrderByField("${field.db_name}"); }
        </#list>
        }

        public static class ColumnLimiterField extends CommonField {
            public ColumnLimiterField(QueryStructure queryStructure) { super(queryStructure); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.OTHER_FIELD; }

        <#list m.columns as field>
            public List${"<"}${field.javaTypeStr}> ${field.propertyName}() { return super.getColumn(createField("${field.db_name}"), ${field.javaTypeStr}.class); }
        </#list>
        }

        public static class ColumnsLimiterField extends CommonField {
            public ColumnsLimiterField(QueryStructure queryStructure) { super(queryStructure); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.OTHER_FIELD; }

            private ColumnsLimiterField createColumnsLimiterField(String column) {
                final QueryStructure oldQueryStructure = getQueryStructure();
                final QueryStructure newQueryStructure = oldQueryStructure.copy(
                        oldQueryStructure.getAction(),
                        new ArrayList<>(oldQueryStructure.getFields()) {{
                            add(createField(column));
                        }},
                        oldQueryStructure.getFrom(),
                        oldQueryStructure.getWhere(),
                        oldQueryStructure.getOrderBy(),
                        oldQueryStructure.getLimit()
                );
                return new ColumnsLimiterField(newQueryStructure);
            }

        <#list m.columns as field>
            public ColumnsLimiterField ${field.propertyName}() { return createColumnsLimiterField("${field.db_name}"); }
        </#list>
        }

        public static class FieldsGenerator extends FieldGenerator {
            @NotNull
            @Override
            public String getTableName() { return TABLE_NAME; }

        <#list m.columns as field>
            public FieldsGenerator ${field.propertyName}() { this.getFields().add(createField("${field.db_name}")); return this; }
        </#list>
        }
    }
}

