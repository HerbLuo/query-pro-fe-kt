<#-- @ftlvariable name="m" type="cn.cloudself.query.util.TemplateModel" -->
<#assign ClassName = m._ClassName/>
<#assign EntityName = m._EntityName/>
<#assign IdType = (m.id.javaTypeStr)!"Long"/>
package ${m.packagePath};

import ${m.entityPackage}.${EntityName};
<#if m.hasBigDecimal>import java.math.BigDecimal;
</#if><#if m.hasDate>import java.util.Date;
</#if>import cn.cloudself.query.*;
import cn.cloudself.query.exception.IllegalCall;
import cn.cloudself.query.util.ListEx;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ${ClassName} {
    private static QueryStructure defQueryStructure() {
        final QueryStructure queryStructure = new QueryStructure();
        queryStructure.setFrom(new QueryStructureFrom(__Impl.TABLE_NAME, new ArrayList<>()));
        return queryStructure;
    }

    private static QueryPro<
            ${EntityName},
            ${IdType},
            __Impl.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.OrderByField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.UpdateSetField,
            __Impl.WhereField${"<"}Boolean, Boolean>,
            __Impl.WhereField${"<"}Boolean, Boolean>
    > createQuery(QueryStructure queryStructure) {
        return new QueryPro<>(
                ${EntityName}.class,
                queryStructure, 
                qs -> new __Impl.WhereField<>(qs, ${EntityName}.class),
                qs -> new __Impl.OrderByField<>(qs, ${EntityName}.class),
                __Impl.UpdateSetField::new,
                qs -> new __Impl.WhereField<>(qs, Boolean.class),
                qs -> new __Impl.WhereField<>(qs, Boolean.class)
        );
    }

    private static final QueryPro<
            ${EntityName},
            ${IdType},
            __Impl.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.OrderByField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.UpdateSetField,
            __Impl.WhereField${"<"}Boolean, Boolean>,
            __Impl.WhereField${"<"}Boolean, Boolean>
    > queryPro = createQuery(defQueryStructure());

    public static final QueryProEx<
            QueryPro<
                    ${EntityName},
                    ${IdType},
                    __Impl.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
                    __Impl.OrderByField${"<"}${EntityName}, List${"<"}${EntityName}>>,
                    __Impl.UpdateSetField,
                    __Impl.WhereField${"<"}Boolean, Boolean>,
                    __Impl.WhereField${"<"}Boolean, Boolean>
            >,
            __Impl.WhereField${"<"}${EntityName}, List${"<"}${EntityName}>>,
            __Impl.FieldsGenerator
    > EX = new QueryProEx<>(
            defQueryStructure(),
            qs -> new __Impl.WhereField<>(qs, ${EntityName}.class),
            __Impl.FieldsGenerator::new,
            ${ClassName}::createQuery
    );

<#list m.queryProDelegate as di>
<#list di.annotations as annotation>
    ${annotation}
</#list>
    <#--noinspection FtlReferencesInspection-->
    ${di.modifiers} <@di.returnType?interpret /> ${di.method}(<#list di.args as arg><@arg.variableType?interpret /> <#if arg.vararg>...</#if>${arg.variableName}<#sep>, </#list>) {
        <#if di.returnType != 'void'>return </#if>queryPro.${di.method}(<#list di.args as arg>${arg.variableName}<#sep>, </#list>);
    }

</#list>
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

            @NotNull
            @Override
            protected QueryPayload getPayload() { return queryPro.getPayload(); }
        }

        public static class WhereField${"<"}T, RUN_RES> extends CommonField${"<"}T, RUN_RES> {
            public WhereField(QueryStructure queryStructure, Class${"<"}T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.WHERE_FIELD; }

            private QueryKeywords${"<"}WhereField${"<"}T, RUN_RES>> createWhereField(String column) {
                return new QueryKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_where_field());
            }

            private WhereField${"<"}T, RUN_RES> createWhereField(String column, Object[] objs) {
                final QueryKeywords${"<"}WhereField${"<"}T, RUN_RES>> whereField = createWhereField(column);
                return objs.length == 1 ? whereField.equalsTo(objs[0]) : whereField.in(objs);
            }

        <#list m.columns as field>
            @Contract(pure = true)
            public QueryKeywords${"<"}WhereField${"<"}T, RUN_RES>> ${field.propertyName}() { return createWhereField("${field.db_name}"); }
            @Contract(pure = true)
            public WhereField${"<"}T, RUN_RES> ${field.propertyName}(List<${field.javaTypeStr}> ${field.propertyName}List) { return createWhereField("${field.db_name}", ${field.propertyName}List.toArray(new Object[0])); }
            @Contract(pure = true)
            public WhereField${"<"}T, RUN_RES> ${field.propertyName}(${field.javaTypeStr}... ${field.propertyName}s) { return createWhereField("${field.db_name}", ${field.propertyName}s); }
        </#list>

            @Contract(pure = true)
            public WhereField${"<"}T, RUN_RES> take(Function${"<"}WhereField${"<"}T, RUN_RES>, WhereField${"<"}T, RUN_RES>> factor) {
                return factor.apply(this);
            }
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
            @Contract(pure = true)
            public QueryOrderByKeywords${"<"}OrderByField${"<"}T, RUN_RES>> ${field.propertyName}() { return createOrderByField("${field.db_name}"); }
        </#list>

            @Contract(pure = true)
            public OrderByField${"<"}T, RUN_RES> take(Function${"<"}OrderByField${"<"}T, RUN_RES>, OrderByField${"<"}T, RUN_RES>> factor) {
                return factor.apply(this);
            }
        }

        public static class ColumnLimiterField${"<"}T, RUN_RES> extends CommonField${"<"}T, RUN_RES> {
            public ColumnLimiterField(QueryStructure queryStructure, Class${"<"}T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.OTHER_FIELD; }

        <#list m.columns as field>
            public ListEx${"<"}${field.javaTypeStr}> ${field.propertyName}() { return new ListEx<>(super.getColumn(createField("${field.db_name}"), ${field.javaTypeStr}.class)); }
        </#list>

            public ${"<"}R> R take(Function${"<"}ColumnLimiterField${"<"}T, RUN_RES>, R> factor) {
                return factor.apply(this);
            }
        }

        public static class ColumnsLimiterField${"<"}T, RUN_RES> extends CommonField${"<"}T, RUN_RES> {
            public ColumnsLimiterField(QueryStructure queryStructure, Class${"<"}T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.OTHER_FIELD; }

            @SuppressWarnings("DuplicatedCode")
            private ColumnsLimiterField${"<"}T, RUN_RES> createColumnsLimiterField(String column) {
                final QueryStructure oldQueryStructure = getQueryStructure();
                final QueryStructure newQueryStructure = oldQueryStructure.copy(
                        oldQueryStructure.getAction(),
                        oldQueryStructure.getUpdate(),
                        new ArrayList${"<"}Field>(oldQueryStructure.getFields()) {{
                            add(createField(column));
                        }},
                        oldQueryStructure.getFrom(),
                        oldQueryStructure.getWhere(),
                        oldQueryStructure.getOrderBy(),
                        oldQueryStructure.getLimit()
                );
                return new ColumnsLimiterField${"<"}>(newQueryStructure, super.getField_clazz());
            }

        <#list m.columns as field>
            @Contract(pure = true)
            public ColumnsLimiterField${"<"}T, RUN_RES> ${field.propertyName}() { return createColumnsLimiterField("${field.db_name}"); }
        </#list>

            @Contract(pure = true)
            public ColumnsLimiterField${"<"}T, RUN_RES> take(Function${"<"}ColumnsLimiterField${"<"}T, RUN_RES>, ColumnsLimiterField${"<"}T, RUN_RES>> factor) {
                return factor.apply(this);
            }
        }

        public static class UpdateSetField extends UpdateField${"<"}WhereField${"<"}Boolean, Boolean>> {
            private final QueryStructure queryStructure;
            public UpdateSetField(QueryStructure queryStructure) {
                super(queryStructure, qs -> new WhereField<>(qs, Boolean.class));
                this.queryStructure = queryStructure;
            }

            @SuppressWarnings("DuplicatedCode")
            private UpdateSetField createUpdateSetField(String key, Object value) {
                final Update update = queryStructure.getUpdate();
                if (update == null) {
                    throw new IllegalCall("usage like: UserQueryPro.updateSet().id(1).name(name).run()");
                }
                @SuppressWarnings("unchecked") final Map${"<"}String, Object> map = (Map${"<"}String, Object>) update.getData();
                assert map != null;
                map.put(key, value);
                return this;
            }

        <#list m.columns as field>
            @Contract(pure = true)
            public UpdateSetField ${field.propertyName}(Object ${field.propertyName}) { return createUpdateSetField("${field.db_name}", ${field.propertyName}); }
        </#list>

            @Contract(pure = true)
            public UpdateSetField take(Function${"<"}UpdateSetField, UpdateSetField> factor) {
                return factor.apply(this);
            }
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

