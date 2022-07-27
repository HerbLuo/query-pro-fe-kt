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
                
                SELECT id AS `a"a'a?a;a'` /*?*/ FROM user;
                SELECT id AS `a"a'a?a;a'` /*?*/ FROM user WHERE id = ?;
                
                SELECT id AS `a"a'a?a;a'` /*?*/ FROM user; # ?
                SELECT id AS `a"a'a?a;a'` /*?*/ FROM user; # */ ?
                SELECT id AS `a"a'a?a;a'` /*?*/ FROM user; -- */ ?
                SELECT id AS `a"a'a?a;a'` /*#?*/ FROM user; -- */ ?
                SELECT id AS `a"a'a?a;a'` /*--?*/ FROM user; -- */ ?
                SELECT id AS `a"a'a?a;a'` /**?*/ FROM user; -- */ ?
                SELECT id AS `a"a'a?a;a'` /**?**/ FROM user; -- */ ?
            """.trimIndent()
        )

        var i = 0
        assertEquals(sqlAndCount.size, 17)

        assertEquals(sqlAndCount[i++].second, 0)

        assertEquals(sqlAndCount[i++].second, 0)

        assertEquals(sqlAndCount[i++].second, 1)
        assertEquals(sqlAndCount[i++].second, 1)
        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)

        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 1)

        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)
        assertEquals(sqlAndCount[i++].second, 0)

        println(i)
    }
}
