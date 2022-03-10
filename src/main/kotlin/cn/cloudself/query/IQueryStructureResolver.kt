package cn.cloudself.query

import org.intellij.lang.annotations.Language

interface IQueryStructureResolver {
    /**
     * 将QueryStructure解析至SQL并执行
     *
     * @param queryStructure [QueryStructure]
     * @param clazz [SupportedQueryClazz]
     */
    fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T>

    /**
     * 执行一个SQL查询
     *
     * @param sql 单条sql语句 e.g. SELECT * FROM user WHERE user.id = ?
     * @param params 参数数组 e.g. [1]
     * @param clazz [SupportedQueryClazz] for select; [Int] for update
     * @param type [QueryStructureAction]
     */
    fun <T> resolve(@Language("SQL") sql: String, params: Array<Any?>, clazz: Class<T>, type: QueryStructureAction): List<T>

    /**
     * 执行一次插入操作
     *
     * @param objs 对象集合
     * @param clazz [SupportedInsertClazz]
     * @return 主键
     */
    fun <T> insert(objs: Collection<Any>, clazz: Class<T>): List<Any>

    /**
     * 执行sql，
     * 根据 数据库连接配置，决定是否能同时执行多条sql
     */
    fun execBatch(sqlArr: Array<String>): IntArray

    /**
     * 使用多条语句和参数执行更新，创建，删除等非select语句
     *
     * @param sqlArr 多条或单条SQL语句
     * @param paramsArr sqlArr 的长度为1时，params的长度任意。代表同一语句包含多参数
     *               sqlArr 的长度不为1时，params的长度必须和 sqlArr的长度相等。
     * @param clazz [SupportedUpdatedBatchClazz]
     */
    fun <T> updateBatch(sqlArr: Array<String>, paramsArr: Array<Array<Any?>>, clazz: Class<T>): T
}
