package cn.cloudself.query

import cn.cloudself.query.exception.IllegalParameters
import cn.cloudself.query.util.SqlUtils
import org.intellij.lang.annotations.Language
import java.io.InputStream

class QueryProSql {
    companion object {
        /**
         * @param sql language=SQL
         */
        @JvmStatic
        @JvmOverloads
        fun create(@Language("SQL") sql: String, params: Array<Any?> = arrayOf()) = Action(sql, params)

        @JvmStatic
        @JvmOverloads
        fun create(inputStream: InputStream, params: Array<Any?> = arrayOf()) = Action(String(inputStream.readBytes()), params)

        @JvmStatic
        @JvmOverloads
        fun createFromClassPath(path: String, params: Array<Any?> = arrayOf()) =
            QueryProSql::class.java.classLoader.getResourceAsStream(path)?.let {
                Action(String(it.readBytes()), params)
            } ?: throw IllegalParameters("路径{0}可能不是标准的ClassPath", path)

        /**
         * 使用多条语句和参数执行更新，创建，删除等非select语句
         *
         * @param sqlAndParams Pair<SQL, Params>[]
         */
        @JvmStatic
        fun createBatch(sqlAndParams: Array<Pair<String, Array<Any?>>>): BatchAction {
            val size = sqlAndParams.size
            val emptyArr = arrayOf<Any?>()
            val sqlArr = Array(size) { "" }
            val paramsArr = Array(size) { emptyArr }
            var i = 0
            for ((sql, params) in sqlAndParams) {
                sqlArr[i] = sql
                paramsArr[i] = params
                i++
            }
            return BatchAction(sqlArr, paramsArr)
        }

        /**
         * 使用多条语句和参数执行更新，创建，删除等非select语句
         *
         * @param sqlArr 多条sql语句
         * @param params 参数数组数组，长度必须和sqlArr一支
         */
        @JvmStatic
        fun createBatch(sqlArr: Array<String>, params: Array<Array<Any?>> = arrayOf()): BatchAction {
            if (sqlArr.size != params.size && sqlArr.size != 1) {
                throw IllegalParameters("sqlArr的长度必须和params的长度一致")
            }
            return BatchAction(sqlArr, params)
        }

        /**
         * 使用单条语句和多组参数执行更新，创建，删除等非select语句
         *
         * @param sql 单条sql语句 e.g. INSERT INTO user (id, name) VALUES (?, ?)
         * @param params 参数数组数组 e.g. [[1, 'hb'], [2, 'l']]
         */
        @JvmStatic
        fun createBatch(@Language("SQL") sql: String, params: Array<Array<Any?>>): BatchAction {
            return BatchAction(arrayOf(sql), params)
        }

        /**
         * 使用含;的复合语句组合执行更新操作,
         * 该语句会对传入的sql按照;进行拆分
         *
         * @param sqlGroup 复合sql语句 e.g. INSERT INTO user (id, name) VALUES (?, ?); UPDATE user SET name = UPPER(name) WHERE id = ?
         * @param params e.g. [1, 'hb, 1]
         */
        @JvmStatic
        @JvmOverloads
        fun createBatchBySqlGroup(@Language("SQL") sqlGroup: String, params: Array<Any?> = arrayOf()): BatchAction {
            val sqlAndCountArr = SqlUtils.splitBySemicolonAndCountQuestionMark(sqlGroup)
            val size = sqlAndCountArr.size
            val sqlArr = Array(size) { "" }
            val emptyArr = arrayOf<Any?>()
            val paramsArr = Array(size) { emptyArr }
            var i = 0
            var j = 0
            for ((sql, count) in sqlAndCountArr) {
                sqlArr[i] = sql
                paramsArr[i] = params.copyOfRange(j, count)
                i++
                j += count
            }
            return BatchAction(sqlArr, paramsArr)
        }
    }

    class BatchAction(
        private val sqlArr: Array<String>,
        private val params: Array<Array<Any?>>
    ) {
        /**
         * 批量更新
         * @param clazz [SupportedUpdatedBatchClazz]
         */
        fun <T> update(clazz: Class<T>): T {
            return QueryProConfig.QueryStructureResolver.updateBatch(sqlArr, params, clazz)
        }

        /**
         * 批量更新
         */
        fun update(): Int {
            return update(Int::class.java)
        }
    }

    class Action(
        private val sql: String,
        private val params: Array<Any?>,
    ) {
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

        /**
         * 使用单条语句执行更新，创建，删除等非select语句
         */
        fun update(): Int {
            return QueryProConfig.QueryStructureResolver.resolve(sql, params, Int::class.java, QueryStructureAction.UPDATE)[0]
        }
    }
}
