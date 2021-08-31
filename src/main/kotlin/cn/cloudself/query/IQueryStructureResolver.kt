package cn.cloudself.query

interface IQueryStructureResolver {
    fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T>
}
