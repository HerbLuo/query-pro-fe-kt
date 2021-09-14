package cn.cloudself.misc

import cn.cloudself.query.util.SqlUtils
import org.junit.Test

class SqlUtilTest {
    @Test
    fun test() {
        val sqlAndCount = SqlUtils.splitBySemicolonAndCountQuestionMark(
            """
            SELECT * FROM zz_trans.user_pri WHERE uid = '?';
            
            SELECT * 
            FROM zz_trans.user_pri 
            WHERE uid = '?';
            
            SELECT uid AS '"`?;' FROM zz_trans.user_pri WHERE uid = ?;
            SELECT uid AS 'a"a`a?a;a' FROM zz_trans.user_pri WHERE uid = ? AND deleted = '?';
            SELECT uid AS "'`?;" FROM zz_trans.user_pri;
            SELECT uid AS "a'a`a?a;a" FROM zz_trans.user_pri;
            SELECT uid AS `'"?;` FROM zz_trans.user_pri;
            SELECT uid AS `a"a'a?a;a'` FROM zz_trans.user_pri;
        """.trimIndent()
        )

        println(sqlAndCount)
    }
}
