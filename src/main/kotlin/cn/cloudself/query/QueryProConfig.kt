package cn.cloudself.query

import javax.sql.DataSource

object QueryProConfig {
    private var dataSource: DataSource? = null
    private val dataSourceThreadLocal: ThreadLocal<DataSource?> = ThreadLocal()

    var dryRun: Boolean = false
    var QueryStructureResolver: IQueryStructureResolver = SpringJdbcQueryStructureResolver()

    fun setDataSource(dataSource: DataSource) {
        this.dataSource = dataSource
    }

    fun setDataSourceThreadLocal(dataSource: DataSource) {
        dataSourceThreadLocal.set(dataSource)
    }

    fun getDataSourceOrInit(init: () -> DataSource): DataSource {
        val currentThreadDataSource = dataSourceThreadLocal.get()
        if (currentThreadDataSource != null) {
            return currentThreadDataSource
        }
        return dataSource ?: init().also { dataSource = it }
    }
}
