package cn.cloudself.query

object QueryProTransaction {
    @JvmStatic
    fun <R> use(block: () -> R): R {
        return block()
    }
}