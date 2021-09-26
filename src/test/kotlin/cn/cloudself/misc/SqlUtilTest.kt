package cn.cloudself.misc

import cn.cloudself.query.util.SqlUtils
import org.junit.Test
import kotlin.test.assertEquals

class SqlUtilTest {
    @Test
    fun test() {
        val sqlAndCount = SqlUtils.splitBySemicolonAndCountQuestionMark(
            """
            SELECT * FROM user WHERE id = '?';
            
            SELECT * 
            FROM user
            WHERE id = '?';
            
            SELECT id AS '"`?;' FROM user WHERE id = ?;
            SELECT id AS 'a"a`a?a;a' FROM user WHERE id = ? AND name = '?';
            SELECT id AS "'`?;" FROM user;
            SELECT id AS "a'a`a?a;a" FROM user;
            SELECT id AS `'"?;` FROM user;
            SELECT id AS `a"a'a?a;a'` FROM user;
        """.trimIndent()
        )

        assertEquals(sqlAndCount.size, 8)
        assertEquals(sqlAndCount[0].second, 0)
        assertEquals(sqlAndCount[1].second, 0)
        assertEquals(sqlAndCount[2].second, 1)
        assertEquals(sqlAndCount[3].second, 1)
        assertEquals(sqlAndCount[4].second, 0)
        assertEquals(sqlAndCount[5].second, 0)
        assertEquals(sqlAndCount[6].second, 0)
        assertEquals(sqlAndCount[7].second, 0)
    }
}
