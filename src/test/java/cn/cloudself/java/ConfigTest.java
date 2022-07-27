package cn.cloudself.java;

import cn.cloudself.java.helpers.query.Setting;
import cn.cloudself.java.helpers.query.SettingQueryPro;
import cn.cloudself.java.helpers.query.UserQueryPro;
import cn.cloudself.query.QueryProConfig;
import cn.cloudself.query.QueryProConfigDb;
import cn.cloudself.query.structure_reolsver.JdbcQSR;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
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
