package cn.cloudself.query

interface IQueryStructureResolver {
    fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T>

    fun <T> resolve(sql: String, params: List<Any?>, clazz: Class<T>, action: QueryStructureAction?): List<T>
}
