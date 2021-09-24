package cn.cloudself.query.structure_reolsver

import cn.cloudself.query.exception.UnSupportException


data class ParsedObjectColumn(
    val javaName: String,
    val value: Any?,
)

class BeanObjectProxy private constructor(
    private val obj: Any,
    private val parsedClass: ParsedClass?
) {
    companion object {
        fun fromObject(obj: Any) = BeanObjectProxy(obj, if (obj is Map<*, *>) null else parseClass(obj.javaClass))

        fun fromObject(obj: Any, parsedClass: ParsedClass) = BeanObjectProxy(obj, parsedClass)
    }

    fun toSequence(): Sequence<ParsedObjectColumn> {
        val columns = parsedClass?.columns
        if (columns == null) {
            if (obj !is Map<*, *>) {
                throw UnSupportException("无parsedClass，仅允许迭代Map")
            }
            return sequence {
                for ((key, value) in obj) {
                    if (key is String) {
                        yield(ParsedObjectColumn(key, value))
                    } else {
                        throw UnSupportException("不支持非Map<String, *>类型的Map")
                    }
                }
            }
        }

        return sequence {
            if (obj is Map<*, *>) {
                for ((key, column) in columns) {
                    yield(ParsedObjectColumn(key, obj[column]))
                }
            } else {
                for ((key, column) in columns) {
                    yield(ParsedObjectColumn(key, column.getter(obj)))
                }
            }
        }
    }


}
