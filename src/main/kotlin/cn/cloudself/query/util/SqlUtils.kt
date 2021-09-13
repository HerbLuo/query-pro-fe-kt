package cn.cloudself.query.util

object SqlUtils {
    enum class CommentTag {
        NormalOneLine,
        SharpOneLine,
        MultiLine,
    }

    fun splitAndCountQuestionMark(sqlStatement: String): List<Pair<String, Int>> {
        val sqlStatementLength = sqlStatement.length
        val sqlAndQuestionMarkCountList = mutableListOf<Pair<String, Int>>()
        val chars = mutableListOf<Char>()
        var questionMarkCount = 0

        var inComment = false
        var commentTag: CommentTag? = null
        var inString = false
        var quota: Char? = null
        for (i in 0 until sqlStatementLength) {
            val char = chars[i]
            chars.add(char)

            fun lookbehindIsEscape() = chars[i - 1] == '\\' && chars[i - 2] != '\\'

            when (char) {
                '\'', '"', '`' -> if (!inComment) {
                    if (!inString) {
                        inString = true
                        quota = char
                    } else {
                        if (!lookbehindIsEscape()) {
                            inString = false
                            quota = char
                        }
                    }
                }
                '/' -> if (!inString && !inComment) {
                    if (chars[i + 1] == '*') {
                        inComment = true
                        commentTag = CommentTag.MultiLine
                    }
                }
                '*' -> if (inComment && commentTag == CommentTag.MultiLine) {
                    if (!lookbehindIsEscape()) {
                        if (chars[i + 1] == '/') {

                            inComment = false
                            commentTag = null
                        }
                    }
                }
                '-' -> {
                }
            }

            if (char == '?') {
                questionMarkCount++
            }




            if (char == ';') {
                val sql = String(chars.toCharArray())
                sqlAndQuestionMarkCountList.add(sql to questionMarkCount)

                chars.clear()
                questionMarkCount = 0
            }
        }

        return sqlAndQuestionMarkCountList
    }
}
