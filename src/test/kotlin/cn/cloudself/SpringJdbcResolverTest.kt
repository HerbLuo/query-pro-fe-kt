package cn.cloudself

import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.WordQueryPro
import com.alibaba.druid.pool.DruidDataSource
import org.junit.Test
import javax.sql.DataSource

class SpringJdbcResolverTest {
    private fun getDataSource(): DataSource {
        val dataSource = DruidDataSource()
        dataSource.url = "jdbc:mysql://127.0.0.1:3306/zz_trans?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC"
        dataSource.username = "root"
        dataSource.password = "123456"
        dataSource.driverClassName = "com.mysql.cj.jdbc.Driver"
        return dataSource
    }

    @Test
    fun test() {
        QueryProConfig.setDataSource(getDataSource())

        val words = WordQueryPro.selectBy().word.equalsTo("formulae").runAsMap()
        println(words)
    }
}