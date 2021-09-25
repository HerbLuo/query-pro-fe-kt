package cn.cloudself.query

class Pageable<T> private constructor(
    private val count: () -> Int,
    private val query: (first: Int, limit: Int) -> List<T>,
) {
    companion object {
        fun <T> create(count: () -> Int, query: (first: Int, limit: Int) -> List<T>): Pageable<T> {
            return Pageable(count, query)
        }
    }

    fun sequence(pageSize: Int = 1000): Sequence<T> {
        return sequence {
            var page = 0
            while (true) {
                val rows = query(page * pageSize, pageSize)
                if (rows.size < pageSize) {
                    break
                }
                yieldAll(rows)
                page++
            }
        }
    }

    fun total(): Int {
        return count()
    }

    fun limit(first: Int, limit: Int): List<T> {
        return query(first, limit)
    }

    data class TotalCountModePage<T>(
        val page: Int,
        val rows: List<T>,
        val total: Int,
    )

    fun totalCountMode(page: Int, pageSize: Int): TotalCountModePage<T> {
        val totalCount = count()
        val first = (page - 1) * pageSize
        val rows = if (first >= totalCount) listOf() else limit(first, pageSize)
        return TotalCountModePage(page, rows, totalCount)
    }

    data class HasNextModePage<T>(
        val page: Int,
        val rows: List<T>,
        val hasNext: Boolean,
    )

    fun hasNextMode(page: Int, pageSize: Int): HasNextModePage<T> {
        val first = (page - 1) * pageSize
        val rows = limit(first, pageSize + 1)
        return HasNextModePage(page, rows.subList(0, pageSize), rows.size > pageSize)
    }
}