package cn.cloudself.query.util

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * 文件位置解析器，即指示生成的文件应该放在哪里。<br/>
 * 函数参数：filename: 模板文件的名称 DaoKt, EntityKt, DaoJava, EntityJava, SingleFileKt, SingleFileJava等。<br/>
 * 需要返回的是：生成的文件应该放在哪个文件夹里面。
 */
typealias FilePathResolver = (filename: String) -> Path

typealias JavaName = String
/**
 * 将数据库名称转为Java名称
 * 一般数据库名称是下_划_线式的, Java类名是驼峰式的, 建议转换
 * 函数参数: dbName 数据库中的名称, type 表或列
 * 需要返回的是
 */
typealias NameConverter = (dbName: String, toType: JavaNameType) -> JavaName

enum class JavaNameType {
    ClassName,
    @Suppress("EnumEntryName")
    propertyName,
}

/**
 * 用于生成 [FilePathResolver]
 * @sample cn.cloudself.samples.QueryProFileMakerSample.singleFileMode
 * @sample cn.cloudself.samples.QueryProFileMakePathFromSample
 */
class PathFrom private constructor() {
    companion object {
        /**
         * 使用builder模式创建一个PathFrom, 另外还有两个快捷方法 [PathFrom.ktPackage], [PathFrom.javaPackage]
         */
        fun create() = PathFrom()

        /**
         * 指示生成的文件应该放在哪个包下面
         *
         * 注意该方法会自动在包后面加上 dao.zz 或 entity， 如需使用绝对包名，可以加上 [abs]，详见第二个示例
         * @sample cn.cloudself.samples.QueryProFileMakerSample.singleFileMode
         * @sample cn.cloudself.samples.QueryProFileMakePathFromSample.ktAbsPackage
         */
        fun ktPackage(packageName: String) = create().ktPackage(packageName).getResolver()

        /**
         * 指示生成的文件应该放在哪个包下面
         * @see ktPackage
         * @sample cn.cloudself.samples.QueryProFileMakePathFromSample.javaPackage
         */
        fun javaPackage(packageName: String) = create().ktPackage(packageName).getResolver()
    }
    private var subModuleName = ""
    private var lang = "kotlin"
    private var packageName = "cn.cloudself"
    private var entityPackage = "entity"
    private var daoPackage = "dao.zz"
    private var abs = false

    /**
     * 如果项目存在子模块, 使用这个设置项目的子模块
     * @sample cn.cloudself.samples.QueryProFileMakePathFromSample.subModule
     */
    fun subModule(subModuleName: String) = this.also { this.subModuleName = subModuleName }

    /**
     * 指示生成的文件应该放在packageName指定的包下面
     * [PathFrom.ktPackage] 会自动在包后面加上 dao 或 entity， 加上abs可以阻止该行为
     * @sample cn.cloudself.samples.QueryProFileMakePathFromSample.ktAbsPackage
     */
    fun abs() = this.also { this.abs = true }

    /**
     * 指示生成的文件应该放在packageName指定的包下面,
     * 存在以下简写 [PathFrom.ktPackage]
     */
    fun ktPackage(packageName: String) = this.also { lang = "kotlin"; this.packageName = packageName }

    /**
     * 指示生成的文件应该放在packageName指定的包下面,
     * 存在以下简写 [PathFrom.javaPackage]
     */
    fun javaPackage(packageName: String) = this.also { lang = "java"; this.packageName = packageName }

    /**
     * 设置生成的entity文件放在哪个包下 默认: entity。
     * 如果是singleFileMode(单文件模式) 该配置不会起任何作用
     * @sample cn.cloudself.samples.QueryProFileMakePathFromSample.entityDaoPackage
     */
    fun entityPackage(entityPackage: String) = this.also { this.entityPackage = entityPackage }

    /**
     * 设置生成的dao文件放在哪个包下 默认: dao.zz
     * @sample cn.cloudself.samples.QueryProFileMakePathFromSample.entityDaoPackage
     */
    fun daoPackage(daoPackage: String) = this.also { this.daoPackage = daoPackage }

    fun getResolver(): FilePathResolver = { filename ->
        val workspace = System.getProperty("user.dir")
        val entityOrDao = when {
            abs -> ""
            filename.startsWith("Entity") -> entityPackage
            else -> daoPackage
        }
        val packagePath = packageNameToPath(packageName + entityOrDao)
        Path(workspace, subModuleName, "src", "main", lang, packagePath)
    }

    private fun packageNameToPath(packageName: String) = packageName.replace(".", File.separator)
}

data class DbInfo(
    val url: String,
    val username: String = "",
    val password: String = "",
    val driver: String = "com.mysql.cj.jdbc.Driver",
)

/**
 * 用于生成[DbInfo], 主要是生成其中的url属性
 * @sample cn.cloudself.samples.QueryProFileMakerDbInfoSample.base
 * @sample cn.cloudself.samples.QueryProFileMakerDbInfoSample.fullUsage
 */
class DbInfoBuilder constructor(
    private val protocol: String,
    private val host: String,
    private val schema: String,
    private var driver: String = "com.mysql.cj.jdbc.Driver"
) {
    companion object {
        fun mysql(host: String, schema: String) = DbInfoBuilder("mysql", host, schema)
    }

    private var port = 3306
    private var params: MutableMap<String, String> = mutableMapOf(
        "useUnicode" to "true",
        "characterEncoding" to "utf-8",
        "serverTimezone" to "Asia/Shanghai",
        "useInformationSchema" to "true",
    )

    /**
     * 设置端口
     */
    fun port(port: Int) = this.also { this.port = port }

    /**
     * 设置驱动
     * [com.mysql.jdbc.Driver]和 mysql-connector-java 5一起用。
     * [com.mysql.cj.jdbc.Driver]和 mysql-connector-java 6一起用。
     */
    fun driver(driver: String) = this.also { this.driver = driver }

    /**
     * 设置连接参数
     *
     * 存在一些默认的参数 [params] 比如使用utf8, 时区+8, useInformationSchema
     */
    fun params(converter: (params: MutableMap<String, String>) -> MutableMap<String, String>) =
        this.also { this.params = converter(this.params) }

    fun toDbInfo(username: String, password: String): DbInfo {
        val params = if (params.isEmpty())
            ""
        else
            "?${this.params.map { (key, value) -> "$key=$value" }.joinToString("&")}"
        return DbInfo("jdbc:$protocol://$host:$port/$schema$params", username, password, driver)
    }
}

val matchFirst_ = "[^_]+_".toRegex()

/**
 * 数据库名称转为Java名称(类名,属性名等)
 *
 * @sample cn.cloudself.samples.QueryProFileMakerDbJavaNameConverterSample
 */
class DbNameToJava private constructor() {
    companion object {
        /**
         * 该默认方法会把
         * 表名 user_info 转换为 UserInfo
         * 列名 create_by 转换为 createBy
         * @see [QueryProFileMaker.dbJavaNameConverter]
         * @sample
         */
        fun createDefault() = DbNameToJava()
    }

    private var preHandles = mutableListOf<(String) -> String>()
    private var preHandle = {db_name: String ->
        var result = db_name
        for (preHandle in preHandles) {
            result = preHandle(result)
        }
        result
    }
    private var toClassName = {db_name: String ->
        preHandle(db_name).split("_")
            .joinToString("") { s -> if (s.isEmpty()) "" else s[0].uppercaseChar() + s.substring(1) }
    }
    @Suppress("PrivatePropertyName")
    private var to_propertyName = {db_name: String -> toClassName(db_name).let { it[0].uppercaseChar() + it.substring(1) }}

    fun addPrefixBeforeConvertToClassName(prefix: String) = this.also { preHandles.add { prefix + it } }

    fun removePrefixBeforeConvertToClassName() = this.also { preHandles.add { it.replace(matchFirst_, "") } }

    fun addPreHandle(preHandle: (String) -> String) = this.also { preHandles.add(preHandle) }

    fun getConverter(): NameConverter = { db_name, toType ->
        when (toType) {
            JavaNameType.ClassName -> {
                toClassName(db_name)
            }
            JavaNameType.propertyName -> {
                to_propertyName(db_name)
            }
        }
    }
}

class QueryProFileMaker private constructor(
    private val templateFileNameAndPaths: List<Pair<String, Path>>
) {
    companion object {
        /**
         * 生成单个Kotlin文件
         * @param filePathResolver [FilePathResolver] 文件位置解析器，即指示生成的文件应该放在哪里。可使用[PathFrom]生成
         * @sample cn.cloudself.samples.QueryProFileMakerSample.singleFileMode
         */
        fun singleFileMode(filePathResolver: FilePathResolver) =
            QueryProFileMaker(listOf("SingleFileKt.ftl".let { Pair(it, filePathResolver(it.dropLast(4))) }))

        /**
         * 生成entity和dao至两个文件
         * @param filePathResolver [FilePathResolver] 文件位置解析器，即指示生成的文件应该放在哪里。可使用[PathFrom]生成
         * @sample cn.cloudself.samples.QueryProFileMakerSample.entityAndDaoMode
         */
        fun entityAndDaoMode(filePathResolver: FilePathResolver) =
            QueryProFileMaker(listOf(
                "DaoKt.ftl".let { Pair(it, filePathResolver(it.dropLast(4))) },
                "EntityKt.ftl".let { Pair(it, filePathResolver(it.dropLast(4))) }
            ))

        /**
         * 生成entity和dao至两个文件 Java版, 参考 [QueryProFileMaker.entityAndDaoMode]
         * @param filePathResolver [FilePathResolver] 文件位置解析器，即指示生成的文件应该放在哪里。可使用[PathFrom]生成
         * @sample cn.cloudself.samples.QueryProFileMakerSample.javaEntityAndDaoMode
         */
        fun javaEntityAndDaoMode(filePathResolver: FilePathResolver) =
            QueryProFileMaker(listOf(
                "DaoJava.ftl".let { Pair(it, filePathResolver(it.dropLast(4))) },
                "EntityJava.ftl".let { Pair(it, filePathResolver(it.dropLast(4))) }
            ))
    }

    private var db: DbInfo? = null
    private var tables: Array<out String> = arrayOf("*")
    private var replaceMode = false
    private var nameConverter = DbNameToJava.createDefault().getConverter()

    /**
     * 指定db
     * @see [DbInfoBuilder.mysql]
     * @sample cn.cloudself.samples.QueryProFileMakerSample
     */
    fun db(db: DbInfo) = this.also { this.db = db }

    /**
     * 指定需要生成QueryPro文件的表名，默认为"*"，代表所有
     * @sample cn.cloudself.samples.QueryProFileMakerSample.entityAndDaoMode
     */
    fun tables(vararg tables: String) = this.also { this.tables = tables }

    /**
     * 是否替换掉已有的文件 默认false
     * @sample cn.cloudself.samples.QueryProFileMakerSample.entityAndDaoMode
     */
    fun replaceMode(replaceMode: Boolean = true) = this.also { this.replaceMode = replaceMode }

    /**
     * 自定义名称转换器(用于转换数据库table, column名称至java类名，属性名)
     * @param nameConverter [NameConverter]
     * @see [DbNameToJava.createDefault]
     */
    fun dbJavaNameConverter(nameConverter: NameConverter) = this.also { this.nameConverter = nameConverter }

    fun create() {
        println(templateFileNameAndPaths)
    }
}
