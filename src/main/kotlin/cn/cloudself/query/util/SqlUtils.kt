package cn.cloudself.query.util

object SqlUtils {
    enum class CommentTag {
        NormalOneLine,
        HashOneLine,
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

            fun lookbehindIsEscape(from: Int = i) = chars[from - 1] == '\\' && chars[from - 2] != '\\'

            when (char) {
                '\'', '"', '`' -> if (!inComment) {
                    if (!inString) {
                        inString = true
                        quota = char
                    } else {
                        if (quota == char && !lookbehindIsEscape()) {
                            inString = false
                            quota = null
                        }
                    }
                }
                '/' -> if (!inString && !inComment) {
                    if (!inComment) {
                        if (chars[i + 1] == '*') {
                            inComment = true
                            commentTag = CommentTag.MultiLine
                        }
                    } else {
                        if (commentTag == CommentTag.MultiLine) {
                            if (chars[i - 1] == '*') {
                                if (!lookbehindIsEscape(i - 1)) {
                                    inComment = false
                                    commentTag = null
                                }
                            }
                        }
                    }
                }
                '-' -> if (!inString && !inComment) {
                    if (chars[i - 1] == '-') {
                        inComment = true
                        commentTag = CommentTag.NormalOneLine
                    }
                }
                '#' -> if (!inString && !inComment) {
                    inComment = true
                    commentTag = CommentTag.HashOneLine
                }
                '\n' -> if (inComment && commentTag != CommentTag.MultiLine) {
                    inComment = false
                    commentTag = null
                }
            }

            if (char == '?') {
                if (!inComment && !inString) {
                    questionMarkCount++
                }
            }

            if (char == ';') {
                if (!inComment && !inString) {
                    val sql = String(chars.toCharArray())
                    sqlAndQuestionMarkCountList.add(sql to questionMarkCount)

                    chars.clear()
                    questionMarkCount = 0
                }
            }
        }

        return sqlAndQuestionMarkCountList
    }
}
