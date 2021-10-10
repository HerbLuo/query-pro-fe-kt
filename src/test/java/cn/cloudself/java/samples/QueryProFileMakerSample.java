package cn.cloudself.java.samples;

import cn.cloudself.query.util.DbInfoBuilder;
import cn.cloudself.query.util.DbNameToJava;
import cn.cloudself.query.util.PathFrom;
import cn.cloudself.query.util.QueryProFileMaker;
import org.junit.Test;

public class QueryProFileMakerSample {
    @Test
    public void javaEntityAndDaoMode() {
        QueryProFileMaker
//                /* 将文件生成至 <project>/zz-example-api/src/test/java/cn/cloudself/demo/java/dao/zz下 */
//                .javaEntityAndDaoMode(PathFrom.create().subModule("zz-example-api").dirTest("test").javaPackageName("cn.cloudself.demo.java").getResolver())
//                /* 生成到指定package下，推荐使用 */
//                .javaEntityAndDaoMode(PathFrom.javaPackage("cn.cloudself.demo.java"))
                .javaEntityAndDaoMode(PathFrom.ktPackage("cn.cloudself.demo.java"))
                .db(
                        DbInfoBuilder
                                .mysql("127.0.0.1", "query_pro_test")
                                .driver("com.mysql.cj.jdbc.Driver")
                                .port(3306)
                                .params(it -> {
                                    it.remove("allowPublicKeyRetrieval");
                                    return it;
                                })
                                .toDbInfo("root", "123456")
                )
                .tables("setting", "user")
//                .dbJavaNameConverter( // 可选，用于将数据库名称转为Java名称
//                        DbNameToJava
//                                .createDefault()
//                                .removePrefixBeforeConvertToClassName()
//                                .addPrefixBeforeConvertToClassName("zz_")
//                                .addPreHandle(it -> it.getName().startsWith("view_") ? it.getName().replace("view_", "") : it.getName())
//                                .getConverter()
//                )
                .chain()
                .replaceMode()
                .debug()
                .create();
    }
}
