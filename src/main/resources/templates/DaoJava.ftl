package test.cloudself.dao;

import cn.cloudself.query.*;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import test.cloudself.pojo.Setting;

import java.util.ArrayList;
import java.util.List;

public class SettingQueryPro {
    private static QueryStructure defQueryStructure() {
        final QueryStructure queryStructure = new QueryStructure();
        queryStructure.setFrom(new QueryStructureFrom(__Impl.TABLE_NAME, new ArrayList<>()));
        return queryStructure;
    }

    private static final QueryPro${"<"}__Impl.WhereField, __Impl.OrderByField> queryPro = new QueryPro<>(
            defQueryStructure(),
            __Impl.WhereField::new,
            __Impl.OrderByField::new
    );

    public static final QueryProEx<
            QueryPro${"<"}__Impl.WhereField, __Impl.OrderByField>,
            __Impl.WhereField,
            __Impl.FieldsGenerator
    > EX = new QueryProEx<>(
            defQueryStructure(),
            __Impl.WhereField::new,
            __Impl.FieldsGenerator::new,
            (qs) -> SettingQueryPro.queryPro
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
        private static final Class${"<"}Setting> CLAZZ = Setting.class;
        public static final String TABLE_NAME = "setting";

        private static Field createField(String column) {
            return new Field(TABLE_NAME, column);
        }

        public abstract static class CommonField extends QueryField${"<"}Setting, WhereField, OrderByField, ColumnLimiterField, ColumnsLimiterField> {
            public CommonField(QueryStructure queryStructure) {
                super(queryStructure);
            }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, WhereField> getCreate_field() {
                return super.getCreate_field();
            }

            @NotNull
            @Override
            protected Class${"<"}Setting> getField_clazz() {
                return CLAZZ;
            }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, WhereField> getCreate_where_field() {
                return WhereField::new;
            }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, OrderByField> getCreate_order_by_field() {
                return OrderByField::new;
            }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, ColumnLimiterField> getCreate_column_limiter_field() {
                return ColumnLimiterField::new;
            }

            @NotNull
            @Override
            protected Function1${"<"}QueryStructure, ColumnsLimiterField> getCreate_columns_limiter_field() {
                return ColumnsLimiterField::new;
            }
        }

        public static class WhereField extends CommonField {
            public WhereField(QueryStructure queryStructure) {
                super(queryStructure);
            }

            @NotNull
            @Override
            protected QueryFieldType getField_type() {
                return QueryFieldType.WHERE_FIELD;
            }

            private QueryKeywords${"<"}WhereField> createWhereField(String column) {
                return new QueryKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_where_field());
            }

            public QueryKeywords${"<"}WhereField> id() {
                return createWhereField("id");
            }

            public QueryKeywords${"<"}WhereField> kee() {
                return createWhereField("kee");
            }
        }

        public static class OrderByField extends CommonField {
            public OrderByField(QueryStructure queryStructure) {
                super(queryStructure);
            }

            @NotNull
            @Override
            protected QueryFieldType getField_type() {
                return QueryFieldType.ORDER_BY_FIELD;
            }

            private QueryOrderByKeywords${"<"}OrderByField> createOrderByField(String column) {
                return new QueryOrderByKeywords<>(createField(column), super.getQueryStructure(), super.getCreate_order_by_field());
            }

            public QueryOrderByKeywords${"<"}OrderByField> id() {
                return createOrderByField("id");
            }

            public QueryOrderByKeywords${"<"}OrderByField> kee() {
                return createOrderByField("kee");
            }
        }

        public static class ColumnLimiterField extends CommonField {
            public ColumnLimiterField(QueryStructure queryStructure) {
                super(queryStructure);
            }

            @NotNull
            @Override
            protected QueryFieldType getField_type() {
                return QueryFieldType.OTHER_FIELD;
            }

            public List${"<"}Long> id() {
                return super.getColumn(createField("id"), Long.class);
            }

            public List${"<"}String> kee() {
                return super.getColumn(createField("kee"), String.class);
            }
        }

        public static class ColumnsLimiterField extends CommonField {
            public ColumnsLimiterField(QueryStructure queryStructure) {
                super(queryStructure);
            }

            @NotNull
            @Override
            protected QueryFieldType getField_type() {
                return QueryFieldType.OTHER_FIELD;
            }

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

            public ColumnsLimiterField id() {
                return createColumnsLimiterField("id");
            }

            public ColumnsLimiterField kee() {
                return createColumnsLimiterField("kee");
            }
        }

        public static class FieldsGenerator extends FieldGenerator {
            @NotNull
            @Override
            public String getTableName() {
                return TABLE_NAME;
            }

            public FieldsGenerator id() {
                this.getFields().add(createField("id"));
                return this;
            }

            public FieldsGenerator kee() {
                this.getFields().add(createField("kee"));
                return this;
            }
        }
    }
}

