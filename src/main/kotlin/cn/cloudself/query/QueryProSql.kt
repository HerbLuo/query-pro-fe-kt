package cn.cloudself.query

import cn.cloudself.query.exception.IllegalParameters
import java.io.InputStream

class QueryProSql {
    companion object {
        @JvmStatic
        fun create(sql: String, params: List<Any?>) = Action(sql, params)
        fun create(inputStream: InputStream, params: List<Any?>) = Action(String(inputStream.readBytes()), params)
        fun createFromClassPath(path: String, params: List<Any?>) =
            QueryProSql::class.java.classLoader.getResourceAsStream(path)?.let {
                Action(String(it.readBytes()), params)
            } ?: throw IllegalParameters("路径{0}可能不是标准的ClassPath", path)
    }

    class Action(
        private val sql: String,
        private val params: List<Any?>,
    ) {
        /**
         * 执行并获取单个对象
         * clazz: 支持JavaBean, 支持Map, 支持基本类型(Long, String, Date, Enum等, 具体参考[QueryProConfig.addResultSetParser])
         */
        fun <T> execForObj(clazz: Class<T>) = execForList(clazz).getOrNull(0)

        fun <T> execForList(clazz: Class<T>): List<T> {
            return listOf()
        }
    }
}
