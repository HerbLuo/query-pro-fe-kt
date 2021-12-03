package cn.cloudself.query.util

import org.intellij.lang.annotations.Language

object SqlUtils {
    enum class CommentTag {
        NormalOneLine,
        HashOneLine,
        MultiLine,
    }

    fun splitBySemicolonAndCountQuestionMark(@Language("SQL") sqlStatement: String): List<Pair<String, Int>> {
        val sqlAndQuestionMarkCountList = mutableListOf<Pair<String, Int>>()
        val chars = mutableListOf<Char>()
        var questionMarkCount = 0

        var inComment = false
        var commentTag: CommentTag? = null
        var inString = false
        var quota: Char? = null
        for (i in sqlStatement.indices) {
            val char = sqlStatement[i]
            chars.add(char)

            fun lookbehindIsEscape(from: Int = i) = sqlStatement[from - 1] == '\\' && sqlStatement[from - 2] != '\\'

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
                '/' -> if (!inString) {
                    if (!inComment) {
                        if (sqlStatement[i + 1] == '*') {
                            inComment = true
                            commentTag = CommentTag.MultiLine
                        }
                    } else {
                        if (commentTag == CommentTag.MultiLine) {
                            if (sqlStatement[i - 1] == '*') {
                                if (!lookbehindIsEscape(i - 1)) {
                                    inComment = false
                                    commentTag = null
                                }
                            }
                        }
                    }
                }
                '-' -> if (!inString && !inComment) {
                    if (sqlStatement[i - 1] == '-') {
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
                    val sql = String(chars.toCharArray()).trim()
                    if (sql.isEmpty()) {
                        continue
                    }
                    sqlAndQuestionMarkCountList.add(sql to questionMarkCount)

                    chars.clear()
                    questionMarkCount = 0
                }
            }
        }

        return sqlAndQuestionMarkCountList
    }
}
