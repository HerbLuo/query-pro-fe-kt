package cn.cloudself.query.util


object StringUtils {
    fun beginPosOfNonWhitespace(searchIn: String): Int {
        var beginPos = 0
        val inLength = searchIn.length
        while (beginPos < inLength) {
            if (!Character.isWhitespace(searchIn[beginPos])) {
                break
            }
            beginPos++
        }
        return beginPos
    }

    fun startsWithIgnoreCaseAndWs(searchIn: String, searchFor: String): Boolean {
        val beginPos = beginPosOfNonWhitespace(searchIn)
        return startsWithIgnoreCase(searchIn, beginPos, searchFor)
    }

    fun startsWithIgnoreCase(searchIn: String, startAt: Int, searchFor: String): Boolean {
        return searchIn.regionMatches(startAt, searchFor, 0, searchFor.length, ignoreCase = true)
    }
}