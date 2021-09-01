package cn.cloudself.query.structure_reolsver

import cn.cloudself.query.*
import cn.cloudself.query.exception.ConfigException
import cn.cloudself.query.exception.UnSupportException
import cn.cloudself.query.util.SpringUtil
import java.math.BigDecimal
import java.sql.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.sql.DataSource

class JdbcQueryStructureResolver: IQueryStructureResolver {

    override fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T> {
        val (sql, params) = QueryStructureToSql(queryStructure).toSqlWithIndexedParams()

        if (QueryProConfig.dryRun) {
            println(sql)
            println(params)
            return listOf()
        }

        if (QueryProConfig.printSql) {
            println(sql)
            println(params)
        }
        val connection = getConnection()
        val preparedStatement = connection.prepareStatement(sql)

        setParam(preparedStatement, params)

        val proxy = BeanProxy.fromClass(clazz)
        val resultList = mutableListOf<T>()
        val resultSet = preparedStatement.executeQuery()
        while (resultSet.next()) {
            resultList.add(mapRow(proxy, resultSet))
        }
        return resultList
    }

    private fun setParam(preparedStatement: PreparedStatement, params: List<Any?>) {
        for ((i, param) in params.withIndex()) {
            when (param) {
                NULL -> preparedStatement.setNull(i + 1, Types.NULL)
                is BigDecimal     -> preparedStatement.setBigDecimal(i + 1, param)
                is Boolean        -> preparedStatement.setBoolean(i + 1, param)
                is Byte           -> preparedStatement.setByte(i + 1, param)
                is ByteArray      -> preparedStatement.setBytes(i + 1, param)
                is java.util.Date -> preparedStatement.setDate(i + 1, java.sql.Date(param.time))
                is java.sql.Date  -> preparedStatement.setDate(i + 1, param)
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
                is Time           -> preparedStatement.setTime(i + 1, param)
                is Timestamp      -> preparedStatement.setTimestamp(i + 1, param)
                else -> throw UnSupportException("equalsTo, in, between等操作传入了不支持的类型{0}", param)
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
                for ((tester, jt) in QueryProConfig.dbColumnInfoToJavaType) {
                    if (tester(DbColumnInfo(columnType, columnName))) {
                        beanNeedType = jt
                        break
                    }
                }
            }

            val value = if (beanNeedType == null) {
                resultSet.getObject(i)
            } else {
                val parser = QueryProConfig.getResultSetParser(beanNeedType)
                if (parser != null) {
                    parser(resultSet)(i) /* value */
                } else {
                    var valueOpt: Optional<Any>? = null
                    for (resultSetParserEx in QueryProConfig.resultSetParserEx) {
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
                                    "使用QueryProConfig(.INSTANCE).addResultSetParser添加转换器",
                                columnName, columnType, beanNeedType.name)
                        }
                    }
                }
            }

            resultProxy.setProperty(columnName, value)
        }

        return resultProxy.toResult()
    }


    private fun getConnection(): Connection {
        val dataSource = QueryProConfig.getDataSourceOrInit {
            SpringUtil.getBean(DataSource::class.java)
                ?: throw ConfigException("无法找到DataSource, 使用QueryProConfig(.INSTANCE).setDataSource设置")
        }
        return dataSource.connection
    }
}