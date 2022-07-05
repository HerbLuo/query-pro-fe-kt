package cn.cloudself.query

import cn.cloudself.query.exception.IllegalImplements
import cn.cloudself.query.util.LogFactory
import java.sql.Connection

object QueryProTransaction {
    private val logger = LogFactory.getLog(QueryProTransaction::class.java)
    internal var isActualTransactionActive = false
    internal val connectionThreadLocal = ThreadLocal<Connection?>()

    @JvmStatic
    fun <R> use(block: () -> R): R {
        isActualTransactionActive = true
        try {
            return block()
        } catch (e: Exception) {
            logger.warn("遇到错误，准备回滚中")
            val connection = connectionThreadLocal.get() ?: throw IllegalImplements(e, "此时connectionThreadLocal不会获取不到，除非未配置dataSource")
            connection.rollback()
            logger.info("回滚完毕")
            throw e
        } finally {
            connectionThreadLocal.get().also {
                val connection = it ?: return@also
                connection.commit()
                connection.autoCommit = true
            }
            connectionThreadLocal.set(null)
            isActualTransactionActive = false
        }
    }
}