package cn.cloudself.java;

import cn.cloudself.query.QueryProConfig;
import cn.cloudself.query.QueryProConfigDb;
import org.junit.Test;

import static cn.cloudself.helpers.HelpersKt.initLogger;

public class ConfigTest {
    @Test
    public void test() {
        initLogger();
        QueryProConfig.context.use(this::noResult);
        QueryProConfig.context.use(this::withResult);
    }

    private void noResult(QueryProConfigDb context) throws Exception { }

    private Boolean withResult(QueryProConfigDb context) throws Exception {
        return true;
    }
}
