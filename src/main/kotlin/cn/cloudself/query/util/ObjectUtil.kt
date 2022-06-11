package cn.cloudself.query.util

import cn.cloudself.query.exception.UnSupportException

data class ParsedObjectColumn(
    val dbName: String,
    val javaName: String,
    val value: Any?,
)

object ObjectUtil {
    @JvmStatic
    @JvmOverloads
    fun toSequence(
        obj: Any,
        parsedClass: ParsedClass? = if (obj is Map<*, *>) null else parseClass(obj.javaClass)
    ): Sequence<ParsedObjectColumn> {
        val columns = parsedClass?.columns
        if (columns == null) {
            if (obj !is Map<*, *>) {
                throw UnSupportException("无parsedClass，仅允许迭代Map")
            }
            return sequence {
                for ((key, value) in obj) {
                    if (key is String) {
                        yield(ParsedObjectColumn(key, key, value))
                    } else {
                        throw UnSupportException("不支持非Map<String, *>类型的Map")
                    }
                }
            }
        }

        return sequence {
            if (obj is Map<*, *>) {
                for ((key, column) in columns) {
                    yield(ParsedObjectColumn(key, column.javaName, obj[column]))
                }
            } else {
                for ((key, column) in columns) {
                    yield(ParsedObjectColumn(key, column.javaName, column.getter(obj)))
                }
            }
        }
    }

    /**
     * 使用Java属性名获取值, 亦支持Map
     */
    @JvmStatic
    fun getValueByPropertyName(obj: Any, propertyName: String): Any? {
        if (obj is Map<*, *>) {
            return obj[propertyName]
        }
        return parseClass(obj.javaClass).getColumnByJavaPropertyName(propertyName)?.getter?.invoke(obj)
    }

    /**
     * 对于@Table对象使用数据库字段名(由@Column注解)获取值
     * 对于其他JavaBean对象使用Java属性名转为snack_case获取值
     * 不支持Map
     *
     * @param fieldName 数据库字段名或snack_case的Java属性名
     */
    @JvmStatic
    fun getValueByFieldName(obj: Any, fieldName: String): Any? {
        return parseClass(obj.javaClass).getColumnDbFieldName(fieldName)?.getter?.invoke(obj)
    }
}