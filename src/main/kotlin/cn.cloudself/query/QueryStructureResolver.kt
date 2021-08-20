package cn.cloudself.query

interface IQueryStructureResolver {
    fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T>
}

class JdbcQueryStructureResolver: IQueryStructureResolver {
    override fun <T> resolve(queryStructure: QueryStructure, clazz: Class<T>): List<T> {
        println(queryStructure)
        return listOf()
    }
}

var QueryStructureResolver: IQueryStructureResolver = JdbcQueryStructureResolver()
