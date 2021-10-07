package cn.cloudself.query.util

import cn.cloudself.query.QueryPro
import cn.cloudself.query.exception.IllegalCall
import cn.cloudself.query.exception.IllegalImplements
import cn.cloudself.query.exception.IllegalTemplate
import cn.cloudself.query.exception.UnSupportException
import freemarker.template.Configuration
import java.io.File
import java.io.InputStream
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.javaMethod

private val logger = LogFactory.getLog(QueryProFileMaker::class.java)

data class JavaFilePath(
    val templateName: String,
    val dir: Path,
    val packagePath: String
)
/**
 * 文件位置解析器，即指示生成的文件应该放在哪里。<br/>
 * 函数参数templateName: 模板文件的名称 DaoKt, EntityKt, DaoJava, EntityJava, SingleFileKt, SingleFileJava等。<br/>
 * 需要返回的是：生成的文件应该放在哪个文件夹里面。
 */
typealias FilePathResolver = (templateName: String) -> JavaFilePath

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
        fun javaPackage(packageName: String) = create().javaPackage(packageName).getResolver()
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

    fun getResolver(): FilePathResolver = { templateName ->
        val workspace = System.getProperty("user.dir")
        val entityOrDao = when {
            abs -> ""
            templateName.startsWith("Entity") -> entityPackage
            else -> daoPackage
        }
        val packagePath = "$packageName.$entityOrDao"
        JavaFilePath(templateName, Path(workspace, subModuleName, "src", "main", lang, packageNameToPath(packagePath)), packagePath)
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

data class ConvertInfo constructor(
    val name: String,
    val toType: JavaNameType,
    val templateName: String
)
typealias JavaName = String
/**
 * 将数据库名称转为Java名称
 * 一般数据库名称是下_划_线式的, Java类名是驼峰式的, 建议转换
 * 函数参数: dbName 数据库中的名称, type 表或列
 * 需要返回的是
 */
typealias NameConverter = (convertInfo: ConvertInfo) -> JavaName

enum class JavaNameType {
    ClassName,
    @Suppress("EnumEntryName")
    propertyName,
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

    private var preHandles = mutableListOf<NameConverter>()
    private var postHandles = mutableListOf<NameConverter>()

    private var preHandle = {convertInfo: ConvertInfo ->
        var name = convertInfo.name
        for (preHandle in preHandles) {
            name = preHandle(convertInfo.copy(name = name))
        }
        name
    }

    private var postHandle = {convertInfo: ConvertInfo ->
        var tName = convertInfo.name
        for (postHandle in postHandles) {
            tName = postHandle(convertInfo.copy(name = tName))
        }
        tName
    }

    fun addPrefixBeforeConvertToClassName(prefix: String) = this.also { preHandles.add { prefix + it.name } }

    fun removePrefixBeforeConvertToClassName() = this.also { preHandles.add { it.name.replace(matchFirst_, "") } }

    fun addSuffixToEntity(suffix: String) = this.also {
        postHandles.add {
            if (it.templateName.startsWith("Entity") && it.toType == JavaNameType.ClassName)
                it.name + suffix
            else
                it.name
        }
    }

    fun addPreHandle(preHandle: NameConverter) = this.also { preHandles.add(preHandle) }

    fun addPostHandle(postHandle: NameConverter) = this.also { postHandles.add(postHandle) }

    fun getConverter(): NameConverter = { convertInfo ->
        preHandle(convertInfo)
            .split("_")
            .joinToString("") { s -> if (s.isEmpty()) "" else s[0].uppercaseChar() + s.substring(1) }
            .let {
                if (convertInfo.toType == JavaNameType.propertyName) {
                    it[0].lowercaseChar() + it.substring(1)
                } else {
                    if (convertInfo.templateName.startsWith("Entity")) {
                        it
                    } else {
                        it + "QueryPro"
                    }
                }
            }
            .let { postHandle(convertInfo.copy(name = it)) }
    }
}

data class TemplateModelColumn(
    var db_name: String,
    var ktTypeStr: String,
    var javaTypeStr: String,
    var remark: String?,

    var propertyName: String? = null,
)

data class ModelId(
    val column: String,
    var autoIncrement: Boolean = false
)

data class TemplateModel(
    var db_name: String,
    var remark: String?,
    var id: ModelId?,
    var hasBigDecimal: Boolean,
    var hasDate: Boolean,

    var entityPackage: String? = null,
    var _EntityName: String? = null,
    var _ClassName: String? = null,
    var packagePath: String? = null,
    var noArgMode: Boolean? = null,
    var columns: List<TemplateModelColumn> = listOf(),
    var queryProDelegate: List<DelegateInfo> = listOf(),
)

data class DelegateInfoArg(
    var variableType: String,
    var variableName: String,
)

data class DelegateInfo(
    var modifiers: String,
    var returnType: String,
    var method: String,
    var args: List<DelegateInfoArg>,
)

class KtJavaType constructor(
    val ktType: String,
    val javaType: String,
) {
    constructor(type: String) : this(type, type)
}

class QueryProFileMaker private constructor(
    private val templateFileNameAndPaths: List<JavaFilePath>
) {
    companion object {
        /**
         * 生成单个Kotlin文件
         * @param filePathResolver [FilePathResolver] 文件位置解析器，即指示生成的文件应该放在哪里。可使用[PathFrom]生成
         * @sample cn.cloudself.samples.QueryProFileMakerSample.singleFileMode
         */
        fun singleFileMode(filePathResolver: FilePathResolver) =
            QueryProFileMaker(listOf(filePathResolver("SingleFileKt.ftl")))

        /**
         * 生成entity和dao至两个文件
         * @param filePathResolver [FilePathResolver] 文件位置解析器，即指示生成的文件应该放在哪里。可使用[PathFrom]生成
         * @sample cn.cloudself.samples.QueryProFileMakerSample.entityAndDaoMode
         */
        fun entityAndDaoMode(filePathResolver: FilePathResolver) =
            QueryProFileMaker(listOf(
                filePathResolver("DaoKt.ftl"),
                filePathResolver("EntityKt.ftl")
            ))

        /**
         * 生成entity和dao至两个文件 Java版, 参考 [QueryProFileMaker.entityAndDaoMode]
         * @param filePathResolver [FilePathResolver] 文件位置解析器，即指示生成的文件应该放在哪里。可使用[PathFrom]生成
         * @sample cn.cloudself.samples.QueryProFileMakerSample.javaEntityAndDaoMode
         */
        fun javaEntityAndDaoMode(filePathResolver: FilePathResolver) =
            QueryProFileMaker(listOf(
                filePathResolver("DaoJava.ftl"),
                filePathResolver("EntityJava.ftl")
            ))
    }

    private var debug = false
    private var db: DbInfo? = null
    private var tables: Array<out String> = arrayOf("*")
    private var replaceMode = false
    private var ktNoArgMode = true
    private var entityFileTemplatePath: String? = null
    private val dbMetaTypeMapKtJavaType = mutableMapOf(
        "BIGINT" to KtJavaType("Long"),
        "VARCHAR" to KtJavaType("String"),
        "CHAR" to KtJavaType("String"),
        "BIT" to KtJavaType("Boolean"),
        "TINYINT" to KtJavaType("Short"),
        "DATETIME" to KtJavaType("Date"),
        "DECIMAL" to KtJavaType("BigDecimal"),
        "DOUBLE" to KtJavaType("Double"),
        "INT" to KtJavaType("Int", "Integer"),
    )
    private val ktKeywords = arrayOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface", "is", "null",
        "object", "package", "return", "super", "this", "throw", "true", "try", "typealias", "typeof", "val", "var",
        "when", "while",
    )
    private var nameConverter = DbNameToJava.createDefault().getConverter()

    /**
     * 显示更多输出
     */
    fun debug() = this.also { this.debug = true }

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
    @JvmOverloads
    fun replaceMode(replaceMode: Boolean = true) = this.also { this.replaceMode = replaceMode }

    /**
     * 关闭Kotlin的no-arg模式
     *
     * kotlin data class配合一些插件，如kotlin-maven-noarg 会自动为data class生成无参构造函数，
     * 如果没有使用这些插件，可以使用该方法显示指定所有参数的默认值，这样Kotlin就会自动生成默认的无参构造函数
     */
    fun disableKtNoArgMode(disableKtNoArgMode: Boolean = true) = this.also { this.ktNoArgMode = !disableKtNoArgMode }

    /**
     * 自定义名称转换器(用于转换数据库table, column名称至java类名，属性名)
     * @param nameConverter [NameConverter]
     * @see [DbNameToJava.createDefault]
     */
    fun dbJavaNameConverter(nameConverter: NameConverter) = this.also { this.nameConverter = nameConverter }

    /**
     * 自定义entity的模板
     */
    fun entityFileTemplatePath(path: String) = this.also { this.entityFileTemplatePath = path }

    fun create() {
        val modelsFromDb = getModelsFromDb().debugPrint()

        for ((db_name, model) in modelsFromDb) {
            val entityFilePath = templateFileNameAndPaths.find { it.templateName.startsWith("Entity") }

            for (javaFilePath in templateFileNameAndPaths) {
                javaFilePath.debugPrint()
                val templateName = javaFilePath.templateName

                val noExtTemplateName = templateName.substring(0, templateName.lastIndexOf("."))
                val areKt = noExtTemplateName.endsWith("Kt")

                val ext = if (areKt) ".kt" else ".java"

                @Suppress("LocalVariableName") val ClassName = nameConverter(ConvertInfo(db_name, JavaNameType.ClassName, templateName))

                val data = mutableMapOf<String, Any>("m" to model)

                model._EntityName = nameConverter(ConvertInfo(db_name, JavaNameType.ClassName, "EntityKt.ftl"))
                if (entityFilePath != null) {
                    model.entityPackage = entityFilePath.packagePath
                }

                model._ClassName = ClassName
                model.packagePath = javaFilePath.packagePath
                model.noArgMode = ktNoArgMode
                val templateDir = "templates"
                model.queryProDelegate = delegateQueryPro(
                    templateName
                ) {
                    QueryProFileMaker::class.java.classLoader.getResourceAsStream("$templateDir/$templateName")
                        ?: throw IllegalCall("没有找到相应的模板")
                }.debugPrint()
                for (column in model.columns) {
                    val propertyName =
                        nameConverter(ConvertInfo(column.db_name, JavaNameType.propertyName, templateName))
                    column.propertyName =
                        if (areKt && ktKeywords.contains(propertyName)) {
                            "`$propertyName`"
                        } else {
                            propertyName
                        }
                }

                val configuration = Configuration(Configuration.VERSION_2_3_31)
                configuration.setClassLoaderForTemplateLoading(QueryProFileMaker::class.java.classLoader, templateDir)
                val template = configuration.getTemplate(templateName)
                javaFilePath.dir.createDirectories()
                val openOptions = mutableListOf<OpenOption>(StandardOpenOption.CREATE)
                if (replaceMode) {
                    openOptions.add(StandardOpenOption.TRUNCATE_EXISTING)
                }
                val filePath = Path(javaFilePath.dir.toAbsolutePath().toString(), ClassName + ext)
                val writer = filePath.outputStream(*openOptions.toTypedArray()).writer()
                template.process(data, writer)
            }
        }
    }

    private fun getModelsFromDb(): MutableMap<String, TemplateModel> {
        return db?.let { db ->
            Class.forName(db.driver)
            val tableNameMapTemplateModel = mutableMapOf<String, TemplateModel>()
            val connection = DriverManager.getConnection(db.url, db.username, db.password)
            if (tables.contains("*")) {
                readTableAsModel(connection, null, tableNameMapTemplateModel)
            } else {
                for (table in tables) {
                    readTableAsModel(connection, table, tableNameMapTemplateModel)
                }
            }

            return@let tableNameMapTemplateModel
        } ?: throw RuntimeException("db信息没有注册，参考，需使用.db方法注册")
    }

    private fun readTableAsModel(connection: Connection, tableNamePattern: String?, tableNameMapTemplateModel: MutableMap<String, TemplateModel>) {
        val metaData = connection.metaData
        val catalog = connection.catalog
        val schema = connection.schema

        // metadata https://dev.mysql.com/doc/refman/8.0/en/show-columns.html
        val tableSet = metaData.getTables(catalog, schema, tableNamePattern, arrayOf("TABLE", "VIEW"))

        while (tableSet.next()) {
            val tableName = tableSet.getString("TABLE_NAME")
            val tableRemark = tableSet.getString("REMARKS")

            val modelColumns = mutableListOf<TemplateModelColumn>()

            /**
             * 获取主键
             */
            var id: ModelId? = null
            var idDefined = false
            val primaryKeys = metaData.getPrimaryKeys(catalog, schema, tableName)
            while (primaryKeys.next()) {
                if (idDefined) {
                    id = null
                    logger.warn("[WARN] 目前仍不支持复合主键")
                } else {
                    val columnName = primaryKeys.getString("COLUMN_NAME")
                    id = ModelId(columnName)
                    idDefined = true
                }
            }

            val columnSet = metaData.getColumns(catalog, schema, tableName, null)
            while (columnSet.next()) {
                val columnName = columnSet.getString("COLUMN_NAME") ?: throw RuntimeException("找不到列名")
                val typeName = columnSet.getString("TYPE_NAME")
                val remarks = columnSet.getString("REMARKS")

                val ktJavaType = dbMetaTypeMapKtJavaType[typeName]
                    ?: dbMetaTypeMapKtJavaType[typeName.replace(" UNSIGNED", "")]

                if (id?.column == columnName) {
                    id.autoIncrement = columnSet.getString("IS_AUTOINCREMENT") == "YES"
                }

                modelColumns.add(TemplateModelColumn(
                    db_name = columnName,
                    ktTypeStr = ktJavaType?.ktType ?: throw RuntimeException("找不到数据库类型${typeName}对应的kt类型, 列名$columnName"),
                    javaTypeStr = ktJavaType.javaType,
                    remark = remarks
                ))
            }

            val templateModel = TemplateModel(
                db_name = tableName,
                remark = tableRemark,
                columns = modelColumns,
                id = id,
                hasBigDecimal = modelColumns.find { it.ktTypeStr == "BigDecimal" } != null,
                hasDate = modelColumns.find { it.ktTypeStr == "Date" } != null,
            )
            tableNameMapTemplateModel[tableName] = templateModel
        }
    }

    private fun delegateQueryPro(templateName: String, templateLoader: () -> InputStream): List<DelegateInfo> {
        if (!templateName.startsWith("DaoJava")) {
            return listOf()
        }

        val daoJavaTemplate = String(templateLoader().readAllBytes())
        val result = Regex("static final QueryPro<([\\s\\S]+)>\\s+queryPro\\s+=").find(daoJavaTemplate)
        val actualGenericTypeStr = result?.groups?.get(1)?.value ?: throw IllegalTemplate("找不到queryPro定义的位置")

        val actualGenericTypes = actualGenericTypeStr.split(",\n")
        val genericTypes = QueryPro::class.typeParameters

        if (genericTypes.size != actualGenericTypes.size) {
            throw IllegalTemplate("模板中QueryPro的参数长度与QueryPro的参数长度不一致")
        }
        val genericTypeMapActualGenericType = mutableMapOf<String, String>()
        for ((i, genericType) in genericTypes.withIndex()) {
            genericTypeMapActualGenericType[genericType.name] = actualGenericTypes[i].trim()
        }
        fun toActualType(genericType: String): String? {
            val actualType = genericTypeMapActualGenericType[genericType]
            if (actualType != null) {
                return actualType
            }
            if (genericType.endsWith("[]")) {
                return genericTypeMapActualGenericType[genericType.substring(0, genericType.length - 2)]?.let { "$it[]" }
            }
            if (genericType.endsWith('>')) {
                var allFind = true
                val indexOfLt = genericType.indexOf('<')
                val paramsStr = genericType.substring(indexOfLt + 1, genericType.length - 1)
                val resTypeBuilder = StringBuilder(genericType.substring(0, indexOfLt + 1))

                val sj = StringJoiner(",")
                for (param in paramsStr.split(',')) {
                    val actual = genericTypeMapActualGenericType[param.replace("? extends ", "").trim()]
                    if (actual != null) {
                        sj.add(actual)
                    } else {
                        allFind = false
                        break
                    }
                }

                resTypeBuilder.append('>')
                if (allFind) {
                    return "${genericType.substring(0, indexOfLt + 1)}${sj}>"
                }
            }
            return null
        }

        fun noPackage(className: String) = className.replace("cn.cloudself.query.", "")

        return QueryPro::class.declaredFunctions
            .map {
                val parameters = it.parameters
                val method = it.javaMethod ?: throw UnSupportException("QueryPro Kotlin方法必须有对应的Java方法")

                DelegateInfo(
                    "public",
                    noPackage(toActualType(method.genericReturnType.typeName) ?: method.returnType.typeName),
                    method.name,
                    method.parameters.withIndex().map { (i, param) ->
                        val kParam = parameters[i + 1]
                        val paramType = toActualType(param.parameterizedType.typeName) ?: param.type.typeName
                        DelegateInfoArg(noPackage(paramType), kParam.name ?: "obj$i")
                    }
                )
            }
    }

    private fun <T> T.debugPrint(): T {
        if (debug) {
            if (this is Iterable<*>) {
                this.forEach { logger.info(it) }
            } else {
                logger.info(this)
            }
        }
        return this
    }
}

@Suppress("unused")
private fun ResultSet.print() {
    val metaData = this.metaData
    for (i in 1..metaData.columnCount) {
        val columnName = metaData.getColumnName(i)
        val data = this.getString(columnName)
        logger.info("$columnName:\t $data")
    }
    logger.info("\n")
}
