package cn.cloudself.samples

import cn.cloudself.helpers.initLogger
import cn.cloudself.query.util.DbInfoBuilder
import cn.cloudself.query.util.DbNameToJava
import cn.cloudself.query.util.PathFrom
import cn.cloudself.query.util.QueryProFileMaker
import org.junit.Test

class QueryProFileMakePathFromSample {
    /**
     * 指示生成的文件应该放在packageName指定的包下面
     * [PathFrom.ktPackageName] 会自动在包后面加上 dao 或 entity， 加上abs可以阻止该行为
     */
    fun ktAbsPackage() {
        QueryProFileMaker
            /* 将文件生成至 <project>/src/main/java/cn/cloudself/foo下 */
            .singleFileMode(PathFrom.create().ktPackageName("cn.cloudself.demo").abs().getResolver())
            .create()
    }

    /**
     * 指示生成的文件应该放在packageName指定的包下面
     */
    fun javaPackage() {
        QueryProFileMaker
            /* 将文件生成至 <project>/src/main/java/cn/cloudself/foo/dao/zz下 */
            .singleFileMode(PathFrom.javaPackage("cn.cloudself.demo"))
            .create()
    }

    /**
     * 如果项目存在子模块, 使用这个设置项目的子模块
     */
    fun subModule() {
        QueryProFileMaker
            /* 将文件生成至 <project>/zz-example-api/src/main/kotlin/cn/cloudself/foo/dao/zz下 */
            .singleFileMode(PathFrom.create().subModule("zz-example-api").ktPackageName("cn.cloudself.demo").getResolver())
            .create()
    }

    /**
     * 设置生成的entity, dao文件放在哪个包下
     *
     * 如果是singleFileMode(单文件模式) 针对entity的配置不会起任何作用
     */
    fun entityDaoPackage() {
        /**
         * 将entity文件生成至 <project>/src/main/kotlin/cn/cloudself/foo/beans下
         * 将dao文件生成至 <project>/src/main/kotlin/cn/cloudself/foo/dao下
         * 注意该示例使用了 [QueryProFileMaker.entityAndDaoMode], 所以生成的文件是dao, entity分离的
         */
        val filePathResolver = PathFrom.create()
            .entityPackage("beans")
            .daoPackage("dao")
            .ktPackageName("cn.cloudself.demo")
            .getResolver()
        QueryProFileMaker
            .entityAndDaoMode(filePathResolver)
            .create()
    }
}

class QueryProFileMakerDbInfoSample {
    /**
     * 必要信息
     */
    @Test
    fun base() {
        QueryProFileMaker
            .singleFileMode(PathFrom.ktPackage("cn.cloudself.demo"))
            /* 指定数据源 */
            .db(DbInfoBuilder.mysql("127.0.0.1", "query_pro_test").toDbInfo("root", "123456"))
            .create()
    }

    /**
     * 完整使用方法
     */
    @Test
    fun fullUsage() {
        QueryProFileMaker
            .singleFileMode(PathFrom.ktPackage("cn.cloudself.demo"))
            .db(
                DbInfoBuilder
                    .mysql("127.0.0.1", "query_pro_test")
                    .driver("com.mysql.cj.jdbc.Driver")
                    .port(3306)
                    .params {
                        it["characterEncoding"] = "gbk"
                        it
                    }
                    .toDbInfo("root", "123456")
            )
            .create()
    }
}

class QueryProFileMakerDbJavaNameConverterSample {
    fun fullUsage() {
        QueryProFileMaker
            .singleFileMode(PathFrom.ktPackage("cn.cloudself.demo"))
            .dbJavaNameConverter(
                DbNameToJava
                    .createDefault()
                    /* 有些时候我们创建数据库表的时候，会在表名前添加一些特定含义的前缀，但我不希望生成的Java类名也包含这个前缀 例如 t_user_info 对应的JavaBean文件为UserInfo.java */
                    .removePrefixBeforeConvertToClassName()
                    /* 往生成的Java类名中添加Zz前缀，例如 表user_info对应的JavaBean文件为ZzUserInfo.java */
                    .addPrefixBeforeConvertToClassName("zz_")
                    /* 加一些逻辑在转换前执行 */
//                    .addPreHandle { if (it.startsWith("view_")) it.replace("view_", "") + "_view" else it }
                    .getConverter()
            )
            .create()
    }
}

/**
 * 推荐参考 多文件模式(Kotlin) 中的示例
 * @see entityAndDaoMode
 */
class QueryProFileMakerSample {
    init {
        initLogger()
    }

    /**
     * 单文件模式
     */
    @Test
    fun singleFileMode() {
        QueryProFileMaker
            /* 将文件生成至 <project>/src/main/kotlin/cn/cloudself/foo/dao/zz下 */
            .singleFileMode(PathFrom.create().dirTest("test").ktPackageName("cn.cloudself.helpers.query").getResolver())
            /* 指定数据源 */
            .db(DbInfoBuilder.mysql("127.0.0.1", "query_pro_test").toDbInfo("root", "123456"))
            /* 指定需要生成QueryPro文件的表名, 默认为"*"代表所有 */
            .tables("user", "setting")
            /* 如文件已存在, 替换掉已有的文件 默认跳过已存在的文件 */
            .replaceMode()
            /* 为Entity显示指定所有构造函数参数的默认值, 以便Kotlin自动生成默认的无参构造函数 */
            .disableKtNoArgMode()
            /* 显示更多输出 */
            .debug()
            .create()
    }

    /**
     * 多文件模式(Kotlin)
     */
    @Test
    fun entityAndDaoMode() {
        QueryProFileMaker
            /* 将entity文件生成至 <project>/src/main/kotlin/cn/cloudself/foo/entity下 */
            /* 将dao文件生成至 <project>/src/main/kotlin/cn/cloudself/foo/dao/zz下 */
            .entityAndDaoMode(PathFrom.ktPackage("cn.cloudself.demo"))
            /* 指定数据源 */
            .db(DbInfoBuilder.mysql("127.0.0.1", "query_pro_test").toDbInfo("root", "123456"))
            /* 指定需要生成QueryPro文件的表名, 默认为"*"代表所有 */
            .tables("setting", "user")
            /* 给Entity添加后缀，该步骤是在下划线转驼峰之后进行的 */
            .dbJavaNameConverter(DbNameToJava.createDefault().addSuffixToEntity("Entity").getConverter())
            /* 如文件已存在, 替换掉已有的文件 默认跳过已存在的文件 */
            .replaceMode()
            /* 为Entity显示指定所有构造函数参数的默认值, 以便Kotlin自动生成默认的无参构造函数 */
            .disableKtNoArgMode()
            /* 显示更多输出 */
            .debug()
            .create()
    }

    /**
     * 多文件模式(Java)
     */
    @Test
    fun javaEntityAndDaoMode() {
        val filePathResolver =
            PathFrom.create().dirTest().javaPackageName("cn.cloudself.java.helpers.query").daoPackage("").entityPackage("")
                .getResolver()
        QueryProFileMaker
            /* 将entity文件生成至 <project>/src/main/java/cn/cloudself/demo/entity下 */
            /* 将dao文件生成至 <project>/src/main/java/cn/cloudself/demo/dao/zz下 */
//            .javaEntityAndDaoMode(PathFrom.javaPackage("cn.cloudself.demo"))
            .javaEntityAndDaoMode(filePathResolver)
            /* 指定数据源 */
            .db(DbInfoBuilder.mysql("127.0.0.1", "query_pro_test").toDbInfo("root", "123456"))
            /* 指定需要生成QueryPro文件的表名, 默认为"*"代表所有 */
            .tables("setting", "user")
            /* 生成的JavaBean允许链式调用 */
            .chain()
            /* 如文件已存在, 替换掉已有的文件 默认跳过已存在的文件 */
            .replaceMode()
            /* 显示更多输出 */
            .debug()
            .create()
    }
}
