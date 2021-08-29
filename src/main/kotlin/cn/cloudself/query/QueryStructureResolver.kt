package cn.cloudself.query

import cn.cloudself.query.exception.ConfigException
import cn.cloudself.query.exception.UnSupportException
import cn.cloudself.query.util.SpringUtil
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.lang.Exception
import java.lang.reflect.Constructor
import java.sql.ResultSet
import java.util.HashMap
import java.util.LinkedHashMap
import javax.sql.DataSource

interface IQueryStructureResolver {
    fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T>
}

class SpringJdbcQueryStructureResolver: IQueryStructureResolver {

    override fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T> {
        val namedJdbcTemplate = getNamedJdbcTemplate()

        println(queryStructure)
        val sql = "select * from word"
        val params = mutableMapOf<String, Any>()

        return namedJdbcTemplate.query(sql, params, JdbcRowMapper(clazz))
    }

    class JdbcRowMapper<T>constructor(private val clazz: Class<T>): RowMapper<T> {
        private val createInstance: () -> T

        init {
            if (Map::class.java.isAssignableFrom(clazz)) {
                println(clazz.isAssignableFrom(LinkedHashMap::class.java))
                println(clazz.isAssignableFrom(HashMap::class.java))

                createInstance = {
                    @Suppress("UNCHECKED_CAST")
//                    mutableMapOf<Int, Any>() as T
                    LinkedHashMap<String, Any>() as T
                }
                try {
                    createInstance()
                } catch (e: Exception) {
                    throw UnSupportException("不支持LinkedHashMap<String, Object>以外的Map")
                }
            } else {
                try {
                    val noArgConstructor = clazz.getDeclaredConstructor()
                    createInstance = { noArgConstructor.newInstance() }
                } catch (e: Exception) {
                    throw UnSupportException(e, "{0} 没有找到无参构造函数，该类是一个JavaBean吗, " +
                            "对于Kotlin，需使用kotlin-maven-noarg生成默认的无参构造函数或" +
                            "在生成工具中配置QueryProFileMaker.disableKtNoArgMode()来生成默认的构造函数", clazz)
                }
            }
        }

        override fun mapRow(resultSet: ResultSet, rowNum: Int): T? {
            val result = createInstance()

            val metaData = resultSet.metaData
            val columnCount = metaData.columnCount

            for (i in 1..columnCount) {
                val columnName = metaData.getColumnName(i)

                val value = resultSet.getObject(columnName)

//                val data = this.getString(columnName)
//                println("$columnName:\t $data")
            }

            return result
        }
    }

    private val namedJdbcTemplateThreadLocal = ThreadLocal<NamedParameterJdbcTemplate?>()

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
