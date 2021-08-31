package cn.cloudself.query.structure_reolsver

import cn.cloudself.query.DbColumnInfo
import cn.cloudself.query.IQueryStructureResolver
import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.QueryStructure
import cn.cloudself.query.exception.ConfigException
import cn.cloudself.query.util.SpringUtil
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource

class SpringJdbcQueryStructureResolver: IQueryStructureResolver {
    private val namedJdbcTemplateThreadLocal = ThreadLocal<NamedParameterJdbcTemplate?>()

    override fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T> {
        if (QueryProConfig.dryRun) {
            println(queryStructure)
            return listOf()
        }

        val namedJdbcTemplate = getNamedJdbcTemplate()

        println(queryStructure)
        val sql = "select * from word"
        val params = mutableMapOf<String, Any>()

        return namedJdbcTemplate.query(sql, params, JdbcRowMapper(clazz))
    }

    @Suppress("UNCHECKED_CAST")
    class JdbcRowMapper<T>constructor(clazz: Class<T>): RowMapper<T> {
        private val proxy = BeanProxy.fromClass(clazz)

        override fun mapRow(resultSet: ResultSet, rowNum: Int): T? {
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
    }

    private fun getNamedJdbcTemplate(): NamedParameterJdbcTemplate {
        val namedJdbcTemplate = namedJdbcTemplateThreadLocal.get()
        if (namedJdbcTemplate != null) {
            return namedJdbcTemplate
        }
        val dataSource = QueryProConfig.getDataSourceOrInit {
            SpringUtil.getBean(DataSource::class.java)
                ?: throw ConfigException("无法找到DataSource, 使用QueryProConfig(.INSTANCE).setDataSource设置")
        }
        return NamedParameterJdbcTemplate(dataSource).also { namedJdbcTemplateThreadLocal.set(it) }
    }
}
