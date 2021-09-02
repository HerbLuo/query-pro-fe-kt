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

    private static QueryPro<
            __Impl.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.OrderByField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.WhereField${"<"}Boolean, Boolean>,
            __Impl.WhereField${"<"}Boolean, Boolean>
    > createQuery(QueryStructure queryStructure) {
        return new QueryPro<>(
                queryStructure, 
                qs -> new __Impl.WhereField<>(qs, ${EntityName}.class),
                qs -> new __Impl.OrderByField<>(qs, ${EntityName}.class),
                qs -> new __Impl.WhereField<>(qs, Boolean.class),
                qs -> new __Impl.WhereField<>(qs, Boolean.class)
        );
    }

    private static final QueryPro<
            __Impl.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.OrderByField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.WhereField${"<"}Boolean, Boolean>,
            __Impl.WhereField${"<"}Boolean, Boolean>
    > queryPro = createQuery(defQueryStructure());

    public static final QueryProEx<
            QueryPro<
                    __Impl.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
                    __Impl.OrderByField${"<"}${EntityName}, List${"<"}${EntityName}>>,
                    __Impl.WhereField${"<"}Boolean, Boolean>,
                    __Impl.WhereField${"<"}Boolean, Boolean>
            >,
            __Impl.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.FieldsGenerator
    > EX = new QueryProEx<>(
            defQueryStructure(),
            qs -> new __Impl.WhereField<>(qs, ${EntityName}.class),
            __Impl.FieldsGenerator::new,
            ${EntityName}QueryPro::createQuery
    );

    public static __Impl.WhereField<${EntityName}, List<${EntityName}>> selectBy() {
        return queryPro.selectBy();
    }

    public static __Impl.OrderByField<${EntityName}, List<${EntityName}>> orderBy() {
        return queryPro.orderBy();
    }

    public static __Impl.WhereField${"<"}Boolean, Boolean> deleteBy() {
        return queryPro.deleteBy();
    }

    public static __Impl.WhereField${"<"}Boolean, Boolean> updateBy() {
        return queryPro.updateBy();
    }

    public static class __Impl {
        private static final Class${"<"}${EntityName}> CLAZZ = ${EntityName}.class;
        public static final String TABLE_NAME = "${m.db_name}";
        private static Field createField(String column) { return new Field(TABLE_NAME, column, null); }

        public abstract static class CommonField${"<"}T, RUN_RES>
                extends QueryField${"<"}T, RUN_RES, WhereField${"<"}T, RUN_RES>, OrderByField${"<"}T, RUN_RES>, ColumnLimiterField${"<"}T, RUN_RES>, ColumnsLimiterField${"<"}T, RUN_RES>> {
            public CommonField(QueryStructure queryStructure, Class${"<"}T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, WhereField${"<"}T, RUN_RES>> getCreate_where_field() { return qs -> new WhereField<>(qs, super.getField_clazz()); }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, OrderByField${"<"}T, RUN_RES>> getCreate_order_by_field() { return qs -> new OrderByField<>(qs, super.getField_clazz()); }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, ColumnLimiterField${"<"}T, RUN_RES>> getCreate_column_limiter_field() { return qs -> new ColumnLimiterField<>(qs, super.getField_clazz()); }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, ColumnsLimiterField${"<"}T, RUN_RES>> getCreate_columns_limiter_field() { return qs -> new ColumnsLimiterField<>(qs, super.getField_clazz()); }
        }

        public static class WhereField${"<"}T, RUN_RES> extends CommonField${"<"}T, RUN_RES> {
            public WhereField(QueryStructure queryStructure, Class${"<"}T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.WHERE_FIELD; }

            private QueryKeywords${"<"}WhereField${"<"}T, RUN_RES>> createWhereField(String column) {
                return new QueryKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_where_field());
            }

        <#list m.columns as field>
            public QueryKeywords${"<"}WhereField${"<"}T, RUN_RES>> ${field.propertyName}() { return createWhereField("${field.db_name}"); }
        </#list>
        }

        public static class OrderByField${"<"}T, RUN_RES> extends CommonField${"<"}T, RUN_RES> {
            public OrderByField(QueryStructure queryStructure, Class${"<"}T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.ORDER_BY_FIELD; }

            private QueryOrderByKeywords${"<"}OrderByField${"<"}T, RUN_RES>> createOrderByField(String column) {
                return new QueryOrderByKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_order_by_field());
            }

        <#list m.columns as field>
            public QueryOrderByKeywords${"<"}OrderByField${"<"}T, RUN_RES>> ${field.propertyName}() { return createOrderByField("${field.db_name}"); }
        </#list>
        }

        public static class ColumnLimiterField${"<"}T, RUN_RES> extends CommonField${"<"}T, RUN_RES> {
            public ColumnLimiterField(QueryStructure queryStructure, Class${"<"}T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.OTHER_FIELD; }

        <#list m.columns as field>
            public List${"<"}${field.javaTypeStr}> ${field.propertyName}() { return super.getColumn(createField("${field.db_name}"), ${field.javaTypeStr}.class); }
        </#list>
        }

        public static class ColumnsLimiterField${"<"}T, RUN_RES> extends CommonField${"<"}T, RUN_RES> {
            public ColumnsLimiterField(QueryStructure queryStructure, Class${"<"}T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.OTHER_FIELD; }

            private ColumnsLimiterField${"<"}T, RUN_RES> createColumnsLimiterField(String column) {
                final QueryStructure oldQueryStructure = getQueryStructure();
                final QueryStructure newQueryStructure = oldQueryStructure.copy(
                        oldQueryStructure.getAction(),
                        new ArrayList${"<"}Field>(oldQueryStructure.getFields()) {{
                            add(createField(column));
                        }},
                        oldQueryStructure.getFrom(),
                        oldQueryStructure.getWhere(),
                        oldQueryStructure.getOrderBy(),
                        oldQueryStructure.getLimit()
                );
                return new ColumnsLimiterField${"<"}T, RUN_RES>(newQueryStructure, super.getField_clazz());
            }

        <#list m.columns as field>
            public ColumnsLimiterField${"<"}T, RUN_RES> ${field.propertyName}() { return createColumnsLimiterField("${field.db_name}"); }
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

