package cn.cloudself.query.structure_reolsver

import cn.cloudself.query.exception.UnSupportException
import java.lang.Exception
import javax.persistence.Column
import javax.persistence.Id

private val caches = mutableMapOf<Class<*>, ParsedClass>()

data class ParsedColumn(
    val javaName: String,
    val javaType: Class<*>,
    val setter: (o: Any?, v: Any?) -> Unit,
    val getter: (o: Any?) -> Any?,
    val dbName: String,
)

data class ParsedClass(
    val columns: Map<String, ParsedColumn>,
    val idColumn: String?,
)

fun parseClass(clazz: Class<*>): ParsedClass {
    return caches.getOrPut(clazz) {
        val columns = mutableMapOf<String, ParsedColumn>()
        var idColumn: String? = null
        var idColumnMay: String? = null

        var classOrSuperClass: Class<*>? = clazz
        while (classOrSuperClass != null) {
            for (field in classOrSuperClass.declaredFields) {
                val fieldName = field.name
                val idAnnotation: Id? = field.getAnnotation(Id::class.java)
                val columnAnnotation: Column? = field.getAnnotation(Column::class.java)
                val dbName = columnAnnotation?.name ?: fieldName.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
                if (idAnnotation != null) {
                    if (idColumn != null) {
                        throw UnSupportException("不支持联合主键")
                    }
                    idColumn = fieldName
                }
                if (fieldName == "id") {
                    idColumnMay = fieldName
                }

                val setterMethodName = "set${fieldName[0].uppercaseChar()}${fieldName.substring(1)}"
                val getterMethodName = "get${fieldName[0].uppercaseChar()}${fieldName.substring(1)}"

                val setter = try {
                    clazz.getMethod(setterMethodName, field.type)
                } catch (e: Exception) {
                    null
                }
                val getter = try {
                    clazz.getDeclaredMethod(getterMethodName)
                }  catch (e: Exception) {
                    null
                }

                columns[dbName] = ParsedColumn(
                    javaName = fieldName,
                    javaType = field.type,
                    setter = { o, v ->
                        if (field.canAccess(o)) {
                            field.set(o, v)
                        } else {
                            if (setter == null) {
                                throw UnSupportException("无法访问私有且无setter的属性")
                            }
                            setter.invoke(o, v)
                        }
                    },
                    getter = { o ->
                        if (field.canAccess(o)) {
                            return@ParsedColumn field.get(o)
                        }
                        if (getter == null) {
                            throw UnSupportException("无法访问私有且无getter的属性")
                        }
                        return@ParsedColumn getter.invoke(o)
                    },
                    dbName = dbName,
                )
            }
            classOrSuperClass = classOrSuperClass.superclass
        }

        ParsedClass(columns = columns, idColumn ?: idColumnMay)
    }
}

data class ParsedObjectColumn(
    val javaName: String,
    val value: Any?,
)

fun parseObject(obj: Any): Sequence<ParsedObjectColumn> {
    if (obj is Map<*, *>) {
        return sequence {
            for ((key, value) in obj) {
                if (key is String) {
                    yield(ParsedObjectColumn(key, value))
                } else {
                    throw UnSupportException("不支持非Map<String, *>类型的Map")
                }
            }
        }
    } else {
        val parsedClass = parseClass(obj.javaClass)
        return sequence {
            for ((key, column) in parsedClass.columns) {
                yield(ParsedObjectColumn(key, column.getter(obj)))
            }
        }
    }
}
