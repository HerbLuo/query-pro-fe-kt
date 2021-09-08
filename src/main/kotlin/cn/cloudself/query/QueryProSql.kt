package cn.cloudself.query

import cn.cloudself.query.exception.IllegalParameters
import java.io.InputStream

class QueryProSql {
    companion object {
        @JvmStatic
        fun create(sql: String) = QueryProSqlAction(sql)
        fun create(inputStream: InputStream) = QueryProSqlAction(String(inputStream.readBytes()))
        fun createFromClassPath(path: String) =
            QueryProSql::class.java.classLoader.getResourceAsStream(path)?.let {
                QueryProSqlAction(String(it.readBytes()))
            } ?: throw IllegalParameters("路径{0}可能不是标准的ClassPath", path)
    }
}

class QueryProSqlAction(
    private val sql: String
) {

}
