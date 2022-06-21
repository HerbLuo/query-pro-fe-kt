package cn.cloudself.query.structure_reolsver

import cn.cloudself.query.*
import cn.cloudself.query.exception.ConfigException
import cn.cloudself.query.exception.IllegalParameters
import cn.cloudself.query.exception.UnSupportException
import cn.cloudself.query.util.*
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.lang.StringBuilder
import java.math.BigDecimal
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.sql.DataSource

class JdbcQueryStructureResolver: IQueryStructureResolver {
    private var dataSourceThreadLocal = ThreadLocal<DataSource?>()

    override fun <R> switchDataSource(dataSource: DataSource, resolve: (resolver: IQueryStructureResolver) -> R): R {
        return try {
            dataSourceThreadLocal.set(dataSource)
            resolve(this)
        } finally {
            dataSourceThreadLocal.remove()
        }
    }

    override fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T> {
        var transformedQueryStructure = queryStructure
        if (transformedQueryStructure.action == QueryStructureAction.UPDATE) {
            for (transformer in QueryProConfig.final.lifecycle().beforeUpdateTransformers) {
                transformedQueryStructure = transformer(clazz, transformedQueryStructure).getOrElse {
                    logger.warn("beforeUpdate钩子阻止了本次操作" as Any, it)
                    return emptyList()
                }
            }
        }

        val (sql, params) = QueryStructureToSql(transformedQueryStructure).toSqlWithIndexedParams()

        val callInfo = getCallInfo()

        if (QueryProConfig.final.printSql()) {
            logger.info(callInfo + "\n" + sql)
            logger.info("params: $params")
        } else {
            logger.debug("{0}\n{1}", callInfo, sql)
            logger.debug(params)
        }

        if (QueryProConfig.final.dryRun()) {
            logger.info("dry run mode, skip querying.")
            @Suppress("UNCHECKED_CAST")
            return if (transformedQueryStructure.action == QueryStructureAction.SELECT) {
                listOf()
            } else {
                listOf(true) as List<T>
            }
        }

        val result = resolvePri(sql, params.toTypedArray(), clazz, transformedQueryStructure.action)
        if (QueryProConfig.final.printResult()) {
            logger.info("result: $result")
        }
        return result
    }

    override fun <T> resolve(sql: String, params: Array<Any?>, clazz: Class<T>, type: QueryStructureAction): List<T> {
        return resolvePri(sql, params, clazz, type)
    }

    override fun <T> insert(objs: Collection<Any>, clazz: Class<T>): List<Any> {
        val beanProxy = BeanProxy.fromClass(clazz)
        val bean = beanProxy.newInstance()
        val parsedClass = bean.getParsedClass()
        val columns = parsedClass.columns.values

        var preHandledObjs: Collection<Any> = objs
        for (beforeInsert in QueryProConfig.final.lifecycle().beforeInsertTransformers) {
            @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST")
            preHandledObjs = beforeInsert(beanProxy as BeanProxy<Any, Any>, preHandledObjs).getOrElse {
                logger.warn("beforeInsert钩子阻止了本次操作" as Any, it)
                return emptyList()
            }
        }

        val paramsArr = preHandledObjs.map { obj -> columns.map { col -> col.getter(obj) }.toTypedArray() }.toTypedArray()
        val uniqueInsert = paramsArr.size == 1

        val sqlBuilder = StringBuilder("INSERT INTO `")
        sqlBuilder.append(parsedClass.dbName, "` (")
        var columnFirstAppend = false
        for ((i, col) in columns.withIndex()) {
            if (uniqueInsert && paramsArr[0][i] == null) {
                continue
            }
            if (columnFirstAppend) {
                sqlBuilder.append(", ")
            } else {
                columnFirstAppend = true
            }
            sqlBuilder.append('`', col.dbName, '`')
        }
        sqlBuilder.append(") VALUES (")
        var valueFirstAppend = false
        for (j in columns.indices) {
            if (uniqueInsert && paramsArr[0][j] == null) {
                continue
            }
            if (valueFirstAppend) {
                sqlBuilder.append(", ?")
            } else {
                sqlBuilder.append("?")
                valueFirstAppend = true
            }
        }
        sqlBuilder.append(')')

        val sql = sqlBuilder.toString()
        val idColumnType = parseClass(clazz).idColumnType
        if (idColumnType == null) {
            logger.warn("没有找到主键或其对应的Class, 返回了空结果")
            return listOf()
        }

        val idColumnProxy = BeanProxy.fromClass(idColumnType)

        getConnection().autoUse { connection ->
            val preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            for (params in paramsArr) {
                setParam(preparedStatement, if (uniqueInsert) params.filterNotNull().toTypedArray() else params, OnNull.NULL)
                preparedStatement.addBatch()
            }
            preparedStatement.executeBatch()
            val resultSet = preparedStatement.generatedKeys

            val results = mutableListOf<Any>()
            while (resultSet.next()) {
                results.add(mapRow(idColumnProxy, resultSet))
            }
            return results
        }
    }

    override fun execBatch(sqlArr: Array<String>) {
        return getConnection().autoUse {
            val statement = it.createStatement()
            for (sql in sqlArr) {
                logger.info("sql executing ", sql)
                statement.execute(sql)
            }
        }
    }

    override fun <T> updateBatch(sqlArr: Array<String>, paramsArr: Array<Array<Any?>>, clazz: Class<T>): T {
        val sqlArraySize = sqlArr.size
        val paramsSize = paramsArr.size
        if (sqlArraySize == 0) {
            throw IllegalParameters("sqlArr的长度不能为空")
        }
        if (sqlArraySize != 1 && sqlArraySize != paramsSize) {
            throw IllegalParameters("sqlArr的长度必须为1或者与paramsArr长度一致")
        }

        getConnection().autoUse { connection ->
            val affectRows = if (sqlArraySize == 1) {
                logger.debug("sql长度为1")
                val preparedStatement = connection.prepareStatement(sqlArr[0])
                for (params in paramsArr) {
                    setParam(preparedStatement, params, OnNull.NULL)
                    preparedStatement.addBatch()
                }
                preparedStatement.executeBatch()
            } else {
                val anyParams = paramsArr.any { it.isNotEmpty() }
                if (anyParams) {
                    logger.debug("log: sql的长度大于1且存在参数且参数长度与sql长度相等")
                    val affectRows = IntArray(sqlArraySize) { 0 }
                    for (i in sqlArr.indices) {
                        val sql = sqlArr[i]
                        val params = paramsArr[i]
                        val affectRowNum = connection.prepareStatement(sql)
                            .also { setParam(it, params, OnNull.NULL) }
                            .executeUpdate()
                        affectRows[i] = affectRowNum
                    }
                    affectRows
                } else {
                    logger.debug("log: 不存在参数")
                    val statement = connection.createStatement()
                    for (sql in sqlArr) {
                        statement.addBatch(sql)
                    }
                    statement.executeBatch()
                }
            }

            @Suppress("UNCHECKED_CAST")
            when {
                List::class.java.isAssignableFrom(clazz) -> {
                    val affectRowList: List<Int> = affectRows.toList()
                    return affectRowList as T
                }
                IntArray::class.java.isAssignableFrom(clazz) -> {
                    return affectRows as T
                }
                else -> {
                    if (clazz.compatibleWithBool()) {
                        if (affectRows[0] == 0) {
                            return false as T
                        }
                        return (affectRows.sum() > 0) as T
                    } else if (clazz.compatibleWithInt()) {
                        return affectRows.sum() as T
                    }
                    throw UnSupportException("不支持的class, 目前只支持List::class.java, listOf<Int>().javaClass, Int, Boolean")
                }
            }
        }
    }

    private fun <T> resolvePri(sql: String, params: Array<Any?>, clazz: Class<T>, action: QueryStructureAction): List<T> {
        getConnection().autoUse { connection ->
            logger.info("成功获取到连接")

            val preparedStatement = connection.prepareStatement(sql)

            setParam(preparedStatement, params, if (action == QueryStructureAction.INSERT) OnNull.NULL else OnNull.BREAK)

            val resultList = mutableListOf<T>()

            fun doSelect() {
                val proxy = BeanProxy.fromClass(clazz)
                val resultSet = preparedStatement.executeQuery()
                while (resultSet.next()) {
                    resultList.add(mapRow(proxy, resultSet))
                }
            }

            fun doUpdate() {
                val updatedCount = preparedStatement.executeUpdate()
                @Suppress("UNCHECKED_CAST")
                when {
                    clazz.compatibleWithBool() -> {
                        val success = updatedCount > 0
                        resultList.add(success as T)
                    }
                    clazz.compatibleWithInt() -> {
                        resultList.add(updatedCount as T)
                    }
                    else -> {
                        throw UnSupportException("不支持的class, 目前只支持List::class.java, listOf<Int>().javaClass, Int, Boolean")
                    }
                }
            }

            when (action) {
                QueryStructureAction.SELECT -> doSelect()
                QueryStructureAction.DELETE, QueryStructureAction.UPDATE, QueryStructureAction.INSERT -> doUpdate()
            }

            return resultList
        }
    }

    enum class OnNull {
        BREAK,
        NULL,
    }

    private fun setParam(preparedStatement: PreparedStatement, params: Array<Any?>, onNull: OnNull) {
        for ((i, param) in params.withIndex()) {
            when (param) {
                NULL              -> preparedStatement.setNull(i + 1, Types.NULL)
                is BigDecimal     -> preparedStatement.setBigDecimal(i + 1, param)
                is Boolean        -> preparedStatement.setBoolean(i + 1, param)
                is Byte           -> preparedStatement.setByte(i + 1, param)
                is ByteArray      -> preparedStatement.setBytes(i + 1, param)
                is Time           -> preparedStatement.setTime(i + 1, param)
                is Timestamp      -> preparedStatement.setTimestamp(i + 1, param)
                is java.sql.Date  -> preparedStatement.setTimestamp(i + 1, Timestamp(param.time))
                is java.util.Date -> preparedStatement.setTimestamp(i + 1, Timestamp(param.time))
                is Double         -> preparedStatement.setDouble(i + 1, param)
                is Enum<*>        -> preparedStatement.setString(i + 1, param.name)
                is Float          -> preparedStatement.setFloat(i + 1, param)
                is Int            -> preparedStatement.setInt(i + 1, param)
                is LocalDate      -> preparedStatement.setDate(i + 1, java.sql.Date.valueOf(param))
                is LocalTime      -> preparedStatement.setTime(i + 1, Time.valueOf(param))
                is LocalDateTime  -> preparedStatement.setTimestamp(i + 1, Timestamp.valueOf(param))
                is Long           -> preparedStatement.setLong(i + 1, param)
                is Short          -> preparedStatement.setShort(i + 1, param)
                is String         -> preparedStatement.setString(i + 1, param)
                else -> {
                    if (param == null && onNull == OnNull.NULL) {
                        preparedStatement.setNull(i + 1, Types.NULL)
                    } else {
                        throw UnSupportException("equalsTo, in, between等操作传入了不支持的类型{0}", param)
                    }
                }
            }
        }
    }

    private fun <T> mapRow(proxy: BeanProxy<*, T>, resultSet: ResultSet): T {
        val resultProxy = proxy.newInstance()

        val metaData = resultSet.metaData
        val columnCount = metaData.columnCount

        for (i in 1..columnCount) {
            val columnName = metaData.getColumnName(i)
            val columnType = metaData.getColumnTypeName(i)
            var beanNeedType = resultProxy.getJavaType(columnName)

            if (beanNeedType == null) {
                for ((tester, jt) in QueryProConfig.final.dbColumnInfoToJavaType()) {
                    if (tester(DbColumnInfo(columnType, columnName))) {
                        beanNeedType = jt
                        break
                    }
                }
            }

            val value = if (beanNeedType == null) {
                resultSet.getObject(i)
            } else {
                val parser = QueryProConfig.final.resultSetParser(beanNeedType)
                if (parser != null) {
                    parser(resultSet)(i) /* value */
                } else {
                    var valueOpt: Optional<Any>? = null
                    for (resultSetParserEx in QueryProConfig.final.resultSetParserEx()) {
                        val valueOptMay = resultSetParserEx(resultSet, beanNeedType, i)
                        if (valueOptMay.isPresent) {
                            valueOpt = valueOptMay
                            break
                        }
                    }
                    if (valueOpt != null) {
                        valueOpt.get()
                    } else {
                        // 没有找到生成目标类型的配置，尝试使用数据库默认的类型转换成目标类型，如果不行，则抛出异常
                        val couldConvertClassName = metaData.getColumnClassName(i)
                        if (beanNeedType.isAssignableFrom(Class.forName(couldConvertClassName))) {
                            resultSet.getObject(i)
                        } else {
                            throw ConfigException("不支持将name: {0}, type: {1}转换为{2}, " +
                                    "使用QueryProConfig.global.addResultSetParser添加转换器",
                                columnName, columnType, beanNeedType.name)
                        }
                    }
                }
            }

            resultProxy.setProperty(columnName, if (resultSet.wasNull()) null else value)
        }

        return resultProxy.toResult()
    }

    private fun getConnection(): Connection {
        var dataSource = dataSourceThreadLocal.get() ?: QueryProConfig.final.dataSourceNullable()
        if (dataSource == null) {
            dataSource = try {
                SpringUtils.getBean(DataSource::class.java)
            } catch (e: NoClassDefFoundError) {
                null
            } ?: throw ConfigException("无法找到DataSource, 使用QueryProConfig.setDataSource设置")
            QueryProConfig.global.setDataSource(dataSource)
        }
        return if (isDataSourceUtilsPresent && TransactionSynchronizationManager.isActualTransactionActive()) {
            // 这里是否去除Spring JDBC到依赖更加好? 虽然也不麻烦。
            DataSourceUtils.getConnection(dataSource)
        } else {
            if (QueryProTransaction.isActualTransactionActive) {
                val connection = QueryProTransaction.connectionThreadLocal.get()
                connection ?: dataSource.connection.also {
                    it.autoCommit = false
                    QueryProTransaction.connectionThreadLocal.set(it)
                }
            } else {
                dataSource.connection
            }
        }
    }

    private inline fun <T : AutoCloseable?, R> T.autoUse(block: (T) -> R): R {
        return if (shouldClose()) {
            use(block)
        } else {
            block(this)
        }
    }

    private fun shouldClose() = if (isDataSourceUtilsPresent) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            false
        } else {
            !QueryProTransaction.isActualTransactionActive
        }
    } else (!QueryProTransaction.isActualTransactionActive).also { if (it) logger.info("Will auto close connection.") }

    companion object {
        private val logger = LogFactory.getLog(JdbcQueryStructureResolver::class.java)

        private val isDataSourceUtilsPresent = try {
            Class.forName("org.springframework.jdbc.datasource.DataSourceUtils")
            true
        } catch (e: Throwable) {
            false
        }
    }
}
