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
         * 执行sql
         * 不是特别建议使用该方法，因为可能没有那么必要
         * 用queryOne或executeUpdate替代即可
         *
         * 该方法会对sql进行简单的判定，如果是select，则调用query（具体根据clazz的类型来）
         * 如果是insert, update, delete, create table等语句，会调用executeUpdate
         * 可能存在无法判定的情况，此时，用query或executeUpdate替代
         *
         * @param clazz 支持JavaBean, 支持Map, 支持基本类型(Long, String, Date, Enum等, 具体参考[QueryProConfig.addResultSetParser]),
         */
        fun <T> execute(clazz: Class<T>): T? {
            return QueryProConfig.QueryStructureResolver.resolve(sql, params, clazz, null).getOrNull(0)
        }

        /**
         * 执行更新，创建，删除等非select语句
         *
         * @param clazz 支持的类型有 Boolean::class.java, Int::class.java
         */
        fun <T> executeUpdate(clazz: Class<T>): T {
            return QueryProConfig.QueryStructureResolver.resolve(sql, params, clazz, QueryStructureAction.UPDATE)[0]
        }

        /**
         * 查询单个对象
         *
         * @param clazz 支持JavaBean, 支持Map, 支持基本类型(Long, String, Date, Enum等, 具体参考[QueryProConfig.addResultSetParser])
         */
        fun <T> queryOne(clazz: Class<T>) = query(clazz).getOrNull(0)

        /**
         * 查询多个对象
         *
         * @param clazz 支持JavaBean, 支持Map, 支持基本类型(Long, String, Date, Enum等, 具体参考[QueryProConfig.addResultSetParser])
         */
        fun <T> query(clazz: Class<T>): List<T> {
            return QueryProConfig.QueryStructureResolver.resolve(sql, params, clazz, QueryStructureAction.SELECT)
        }
    }
}
