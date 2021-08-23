package cn.cloudself

import cn.cloudself.query.util.PathFrom
import cn.cloudself.query.util.QueryProFileMaker
import org.junit.Test
import kotlin.io.path.Path

class Misc {
    @Test
    fun test() {
        val path = Path("C:\\Users\\HerbLuo\\Documents\\", "", "java\\doc\\cn\\cloudself", "", "dao")
            .fileSystem
        println(path.toString())
    }
}
