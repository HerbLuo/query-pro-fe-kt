package cn.cloudself;

import cn.cloudself.query.*;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class SettingQueryPro {
    private static QueryStructure defQueryStructure() {
        final QueryStructure queryStructure = new QueryStructure();
        queryStructure.setFrom(new QueryStructureFrom(TABLE_NAME, new ArrayList<>()));
        return queryStructure;
    }

    public static final QueryPro<WhereField, OrderByField> queryPro = new QueryPro<>(
            defQueryStructure(),
            WhereField::new,
            OrderByField::new
    );

//    private static class Impl {
        private static final Class<Setting> CLAZZ = Setting.class;
        public static String TABLE_NAME = "setting";
        private static Field createField(String column) {
            return new Field(TABLE_NAME, column);
        }

        public abstract static class CommonField extends QueryField<Setting, WhereField, OrderByField, ColumnLimiterField, ColumnsLimiterField> {
            public CommonField(QueryStructure queryStructure) {
                super(queryStructure);
            }

            @NotNull
            @Override
            protected Function1<QueryStructure, WhereField> getCreate_field() {
                return super.getCreate_field();
            }

            @NotNull
            @Override
            protected Class<Setting> getField_clazz() {
                return CLAZZ;
            }

            @NotNull
            @Override
            protected Function1<QueryStructure, WhereField> getCreate_where_field() {
                return WhereField::new;
            }

            @NotNull
            @Override
            protected Function1<QueryStructure, OrderByField> getCreate_order_by_field() {
                return OrderByField::new;
            }

            @NotNull
            @Override
            protected Function1<QueryStructure, ColumnLimiterField> getCreate_column_limiter_field() {
                return ColumnLimiterField::new;
            }

            @NotNull
            @Override
            protected Function1<QueryStructure, ColumnsLimiterField> getCreate_columns_limiter_field() {
                return ColumnsLimiterField::new;
            }
        }

        public static class WhereField extends CommonField {
            public WhereField(QueryStructure queryStructure) {
                super(queryStructure);
            }

            @NotNull
            @Override
            public QueryFieldType getField_type() {
                return QueryFieldType.WHERE_FIELD;
            }

//            private QueryKeywords createWhereField(String column) {
//                return new QueryKeywords(createField(column), super.getQueryStructure(), super.getCreate_where_field());
//            }
//
//            public QueryKeywords id() {
//                return createWhereField("id");
//            }
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

//            private QueryOrderByKeywords createOrderByField(String column) {
//                return new QueryOrderByKeywords(createField(column), super.getQueryStructure(), super.getCreate_where_field());
//            }
//
//            public QueryOrderByKeywords id() {
//                return createOrderByField("id");
//            }
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

//            public List<Long> id() {
//                return super.getColumn(createField("id"), Long.class);
//            }
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

//            private ColumnsLimiterField createColumnsLimiterField(String column) {
////                return new ColumnsLimiterField(getQueryStructure().copy() queryStructure.copy(fields = queryStructure.fields + createField(column)))
//            }
//
//            public ColumnsLimiterField id() {
//                return createColumnsLimiterField("id");
//            }
        }

//        class FieldsGenerator: FieldGenerator() {
//            override val tableName = TABLE_NAME
//
//            fun id() = this.also { fields.add(createField("id")) }
//        }
//    }
}
