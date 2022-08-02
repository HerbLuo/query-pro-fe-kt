package cn.cloudself.java;

import cn.cloudself.query.config.QueryProConfig;
import cn.cloudself.query.config.QueryProConfigDb;
import cn.cloudself.query.resolver.JdbcQSR;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static cn.cloudself.helpers.HelpersKt.initLogger;

public class ConfigTest {
    @Test
    public void test() {
        initLogger();
        QueryProConfig.context.use(this::noResult);
        QueryProConfig.context.use(this::withResult);


        QueryProConfig.global.shouldIgnoreFields().add("serialVersionUID");
        QueryProConfig.global.setQueryStructureResolver(new JdbcQSR());
        QueryProConfig.global
                .addResultSetParser(
                        LocalDateTime.class,
                        (resultSet, columIndex) -> resultSet.getTimestamp(columIndex).toLocalDateTime()
                )
                .addResultSetParserEx((resultSet, targetClass, columnIndex) -> {
                    if (!targetClass.isEnum()) {
                        return Optional.empty();
                    }
                    return Optional.of(Enum.valueOf((Class) targetClass, resultSet.getString(columnIndex)));
                });

    }

    private void noResult(QueryProConfigDb context) throws Exception { }

    private Boolean withResult(QueryProConfigDb context) throws Exception {
        return true;
    }
}
