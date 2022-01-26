package cn.cloudself.java.helpers.query;

import cn.cloudself.java.helpers.query.Setting;
import cn.cloudself.query.*;
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
public class SettingQueryPro {
    private static QueryStructure defQueryStructure() {
        final QueryStructure queryStructure = new QueryStructure();
        queryStructure.setFrom(new QueryStructureFrom(__Impl.TABLE_NAME, new ArrayList<>()));
        return queryStructure;
    }

    private static QueryPro<
            Setting,
            Long,
            __Impl.WhereField<Setting, List<Setting>>,
            __Impl.OrderByField<Setting, List<Setting>>,
            __Impl.UpdateSetField,
            __Impl.WhereField<Boolean, Boolean>,
            __Impl.WhereField<Boolean, Boolean>
    > createQuery(QueryStructure queryStructure) {
        return new QueryPro<>(
                Setting.class,
                queryStructure, 
                qs -> new __Impl.WhereField<>(qs, Setting.class),
                qs -> new __Impl.OrderByField<>(qs, Setting.class),
                __Impl.UpdateSetField::new,
                qs -> new __Impl.WhereField<>(qs, Boolean.class),
                qs -> new __Impl.WhereField<>(qs, Boolean.class)
        );
    }

    private static final QueryPro<
            Setting,
            Long,
            __Impl.WhereField<Setting, List<Setting>>,
            __Impl.OrderByField<Setting, List<Setting>>,
            __Impl.UpdateSetField,
            __Impl.WhereField<Boolean, Boolean>,
            __Impl.WhereField<Boolean, Boolean>
    > queryPro = createQuery(defQueryStructure());

    public static final QueryProEx<
            QueryPro<
                    Setting,
                    Long,
                    __Impl.WhereField<Setting, List<Setting>>,
                    __Impl.OrderByField<Setting, List<Setting>>,
                    __Impl.UpdateSetField,
                    __Impl.WhereField<Boolean, Boolean>,
                    __Impl.WhereField<Boolean, Boolean>
            >,
            __Impl.WhereField<Setting, List<Setting>>,
            __Impl.FieldsGenerator
    > EX = new QueryProEx<>(
            defQueryStructure(),
            qs -> new __Impl.WhereField<>(qs, Setting.class),
            __Impl.FieldsGenerator::new,
            SettingQueryPro::createQuery
    );

    public static __Impl.WhereField<Boolean, Boolean> deleteBy() {
        return queryPro.deleteBy();
    }

    public static boolean deleteByPrimaryKey(Object keyValue) {
        return queryPro.deleteByPrimaryKey(keyValue);
    }

    public static Long insert(Setting obj) {
        return queryPro.insert(obj);
    }

    public static java.util.List<Long> insert(Setting ...objs) {
        return queryPro.insert(objs);
    }

    public static java.util.List<Long> insert(java.util.Collection<Setting> collection) {
        return queryPro.insert(collection);
    }

    public static __Impl.OrderByField<Setting, List<Setting>> orderBy() {
        return queryPro.orderBy();
    }

    public static __Impl.WhereField<Setting, List<Setting>> selectAll() {
        return queryPro.selectAll();
    }

    public static __Impl.WhereField<Setting, List<Setting>> selectBy() {
        return queryPro.selectBy();
    }

    public static Setting selectByPrimaryKey(Object value) {
        return queryPro.selectByPrimaryKey(value);
    }

    public static __Impl.UpdateSetField updateSet() {
        return queryPro.updateSet();
    }

    public static UpdateField<__Impl.WhereField<Boolean, Boolean>> updateSet(Setting obj, boolean override) {
        return queryPro.updateSet(obj, override);
    }

    public static class __Impl {
        private static final Class<Setting> CLAZZ = Setting.class;
        public static final String TABLE_NAME = "setting";
        private static Field createField(String column) { return new Field(TABLE_NAME, column, null); }

        public abstract static class CommonField<T, RUN_RES>
                extends QueryField<T, RUN_RES, WhereField<T, RUN_RES>, OrderByField<T, RUN_RES>, ColumnLimiterField<T, RUN_RES>, ColumnsLimiterField<T, RUN_RES>> {
            public CommonField(QueryStructure queryStructure, Class<T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected Function1<QueryStructure, WhereField<T, RUN_RES>> getCreate_where_field() { return qs -> new WhereField<>(qs, super.getField_clazz()); }

            @NotNull
            @Override
            protected Function1<QueryStructure, OrderByField<T, RUN_RES>> getCreate_order_by_field() { return qs -> new OrderByField<>(qs, super.getField_clazz()); }

            @NotNull
            @Override
            protected Function1<QueryStructure, ColumnLimiterField<T, RUN_RES>> getCreate_column_limiter_field() { return qs -> new ColumnLimiterField<>(qs, super.getField_clazz()); }

            @NotNull
            @Override
            protected Function1<QueryStructure, ColumnsLimiterField<T, RUN_RES>> getCreate_columns_limiter_field() { return qs -> new ColumnsLimiterField<>(qs, super.getField_clazz()); }
        }

        public static class WhereField<T, RUN_RES> extends CommonField<T, RUN_RES> {
            public WhereField(QueryStructure queryStructure, Class<T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.WHERE_FIELD; }

            private QueryKeywords<WhereField<T, RUN_RES>> createWhereField(String column) {
                return new QueryKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_where_field());
            }

            private WhereField<T, RUN_RES> createWhereField(String column, Object[] objs) {
                final QueryKeywords<WhereField<T, RUN_RES>> whereField = createWhereField(column);
                return objs.length == 1 ? whereField.equalsTo(objs[0]) : whereField.in(objs);
            }

            @Contract(pure = true)
            public QueryKeywords<WhereField<T, RUN_RES>> id() { return createWhereField("id"); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> id(List<Long> idList) { return createWhereField("id", idList.toArray(new Object[0])); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> id(Long... ids) { return createWhereField("id", ids); }
            @Contract(pure = true)
            public QueryKeywords<WhereField<T, RUN_RES>> userId() { return createWhereField("user_id"); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> userId(List<Long> userIdList) { return createWhereField("user_id", userIdList.toArray(new Object[0])); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> userId(Long... userIds) { return createWhereField("user_id", userIds); }
            @Contract(pure = true)
            public QueryKeywords<WhereField<T, RUN_RES>> kee() { return createWhereField("kee"); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> kee(List<String> keeList) { return createWhereField("kee", keeList.toArray(new Object[0])); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> kee(String... kees) { return createWhereField("kee", kees); }
            @Contract(pure = true)
            public QueryKeywords<WhereField<T, RUN_RES>> value() { return createWhereField("value"); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> value(List<String> valueList) { return createWhereField("value", valueList.toArray(new Object[0])); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> value(String... values) { return createWhereField("value", values); }
            @Contract(pure = true)
            public QueryKeywords<WhereField<T, RUN_RES>> deleted() { return createWhereField("deleted"); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> deleted(List<Boolean> deletedList) { return createWhereField("deleted", deletedList.toArray(new Object[0])); }
            @Contract(pure = true)
            public WhereField<T, RUN_RES> deleted(Boolean... deleteds) { return createWhereField("deleted", deleteds); }

            @Contract(pure = true)
            public WhereField<T, RUN_RES> take(Function<WhereField<T, RUN_RES>, WhereField<T, RUN_RES>> factor) {
                return factor.apply(this);
            }
        }

        public static class OrderByField<T, RUN_RES> extends CommonField<T, RUN_RES> {
            public OrderByField(QueryStructure queryStructure, Class<T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.ORDER_BY_FIELD; }

            private QueryOrderByKeywords<OrderByField<T, RUN_RES>> createOrderByField(String column) {
                return new QueryOrderByKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_order_by_field());
            }

            @Contract(pure = true)
            public QueryOrderByKeywords<OrderByField<T, RUN_RES>> id() { return createOrderByField("id"); }
            @Contract(pure = true)
            public QueryOrderByKeywords<OrderByField<T, RUN_RES>> userId() { return createOrderByField("user_id"); }
            @Contract(pure = true)
            public QueryOrderByKeywords<OrderByField<T, RUN_RES>> kee() { return createOrderByField("kee"); }
            @Contract(pure = true)
            public QueryOrderByKeywords<OrderByField<T, RUN_RES>> value() { return createOrderByField("value"); }
            @Contract(pure = true)
            public QueryOrderByKeywords<OrderByField<T, RUN_RES>> deleted() { return createOrderByField("deleted"); }

            @Contract(pure = true)
            public OrderByField<T, RUN_RES> take(Function<OrderByField<T, RUN_RES>, OrderByField<T, RUN_RES>> factor) {
                return factor.apply(this);
            }
        }

        public static class ColumnLimiterField<T, RUN_RES> extends CommonField<T, RUN_RES> {
            public ColumnLimiterField(QueryStructure queryStructure, Class<T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.OTHER_FIELD; }

            public ListEx<Long> id() { return new ListEx<>(super.getColumn(createField("id"), Long.class)); }
            public ListEx<Long> userId() { return new ListEx<>(super.getColumn(createField("user_id"), Long.class)); }
            public ListEx<String> kee() { return new ListEx<>(super.getColumn(createField("kee"), String.class)); }
            public ListEx<String> value() { return new ListEx<>(super.getColumn(createField("value"), String.class)); }
            public ListEx<Boolean> deleted() { return new ListEx<>(super.getColumn(createField("deleted"), Boolean.class)); }

            public <R> R take(Function<ColumnLimiterField<T, RUN_RES>, R> factor) {
                return factor.apply(this);
            }
        }

        public static class ColumnsLimiterField<T, RUN_RES> extends CommonField<T, RUN_RES> {
            public ColumnsLimiterField(QueryStructure queryStructure, Class<T> field_clazz) { super(queryStructure, field_clazz); }

            @NotNull
            @Override
            protected QueryFieldType getField_type() { return QueryFieldType.OTHER_FIELD; }

            @SuppressWarnings("DuplicatedCode")
            private ColumnsLimiterField<T, RUN_RES> createColumnsLimiterField(String column) {
                final QueryStructure oldQueryStructure = getQueryStructure();
                final QueryStructure newQueryStructure = oldQueryStructure.copy(
                        oldQueryStructure.getAction(),
                        oldQueryStructure.getUpdate(),
                        new ArrayList<Field>(oldQueryStructure.getFields()) {{
                            add(createField(column));
                        }},
                        oldQueryStructure.getFrom(),
                        oldQueryStructure.getWhere(),
                        oldQueryStructure.getOrderBy(),
                        oldQueryStructure.getLimit()
                );
                return new ColumnsLimiterField<>(newQueryStructure, super.getField_clazz());
            }

            @Contract(pure = true)
            public ColumnsLimiterField<T, RUN_RES> id() { return createColumnsLimiterField("id"); }
            @Contract(pure = true)
            public ColumnsLimiterField<T, RUN_RES> userId() { return createColumnsLimiterField("user_id"); }
            @Contract(pure = true)
            public ColumnsLimiterField<T, RUN_RES> kee() { return createColumnsLimiterField("kee"); }
            @Contract(pure = true)
            public ColumnsLimiterField<T, RUN_RES> value() { return createColumnsLimiterField("value"); }
            @Contract(pure = true)
            public ColumnsLimiterField<T, RUN_RES> deleted() { return createColumnsLimiterField("deleted"); }

            @Contract(pure = true)
            public ColumnsLimiterField<T, RUN_RES> take(Function<ColumnsLimiterField<T, RUN_RES>, ColumnsLimiterField<T, RUN_RES>> factor) {
                return factor.apply(this);
            }
        }

        public static class UpdateSetField extends UpdateField<WhereField<Boolean, Boolean>> {
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
                @SuppressWarnings("unchecked") final Map<String, Object> map = (Map<String, Object>) update.getData();
                assert map != null;
                map.put(key, value);
                return this;
            }

            @Contract(pure = true)
            public UpdateSetField id(Object id) { return createUpdateSetField("id", id); }
            @Contract(pure = true)
            public UpdateSetField userId(Object userId) { return createUpdateSetField("user_id", userId); }
            @Contract(pure = true)
            public UpdateSetField kee(Object kee) { return createUpdateSetField("kee", kee); }
            @Contract(pure = true)
            public UpdateSetField value(Object value) { return createUpdateSetField("value", value); }
            @Contract(pure = true)
            public UpdateSetField deleted(Object deleted) { return createUpdateSetField("deleted", deleted); }

            @Contract(pure = true)
            public UpdateSetField take(Function<UpdateSetField, UpdateSetField> factor) {
                return factor.apply(this);
            }
        }

        public static class FieldsGenerator extends FieldGenerator {
            @NotNull
            @Override
            public String getTableName() { return TABLE_NAME; }

            public FieldsGenerator id() { this.getFields().add(createField("id")); return this; }
            public FieldsGenerator userId() { this.getFields().add(createField("user_id")); return this; }
            public FieldsGenerator kee() { this.getFields().add(createField("kee")); return this; }
            public FieldsGenerator value() { this.getFields().add(createField("value")); return this; }
            public FieldsGenerator deleted() { this.getFields().add(createField("deleted")); return this; }
        }
    }
}

