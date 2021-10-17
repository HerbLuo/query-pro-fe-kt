package cn.cloudself.query

import cn.cloudself.query.exception.IllegalImplements
import java.sql.Connection

object QueryProTransaction {
    internal var isActualTransactionActive = false
    internal val connectionThreadLocal = ThreadLocal<Connection?>()

    @JvmStatic
    fun <R> use(block: () -> R): R {
        isActualTransactionActive = true
        try {
            return block()
        } catch (e: Exception) {
            val connection = connectionThreadLocal.get() ?: throw IllegalImplements("此时connectionThreadLocal不会获取不到")
            connection.rollback()
            throw e
        } finally {
            connectionThreadLocal.get().also { it?.autoCommit = true }
            connectionThreadLocal.set(null)
            isActualTransactionActive = false
        }
    }
}