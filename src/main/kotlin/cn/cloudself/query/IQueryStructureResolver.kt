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
     * 使用多条语句和参数执行更新，创建，删除等非select语句
     *
     * @param sqlArr 多条或单条SQL语句
     * @param params 当上述
     * @param clazz [SupportedUpdatedBatchClazz]
     */
    fun <T> updateBatch(sqlArr: Array<String>, params: Array<Array<Any?>>, clazz: Class<T>): T
}
