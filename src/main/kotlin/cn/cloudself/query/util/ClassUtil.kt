package cn.cloudself.query.util

import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.exception.UnSupportException
import javax.persistence.Column
import javax.persistence.Id
import javax.persistence.Table

private val caches = mutableMapOf<Class<*>, ParsedClass>()

data class ParsedColumn(
    val javaName: String,
    val javaType: Class<*>,
    val setter: (o: Any?, v: Any?) -> Unit,
    val getter: (o: Any?) -> Any?,
    val dbName: String,
)

data class ParsedClass(
    val dbName: String,
    val columns: Map<String, ParsedColumn>,
    val idColumn: String?,
    val idColumnType: Class<*>?,
) {
    fun getColumnDbFieldName(fieldName: String) = columns[fieldName]
    fun getColumnByJavaPropertyName(propertyName: String) = columns.values.find { it.javaName == propertyName }
}

@Suppress("FunctionName")
private fun to_snake_case(javaName: String) =
    javaName.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").toLowerCase()

fun parseClass(clazz: Class<*>): ParsedClass {
    return caches.getOrPut(clazz) {
        val columns = mutableMapOf<String, ParsedColumn>()
        var idColumn: String? = null
        var idColumnMay: String? = null
        var idColumnType: Class<*>? = null

        val tableAnnotation: Table? = clazz.getAnnotation(Table::class.java)
        val dbNameForTable = tableAnnotation?.name ?: to_snake_case(clazz.name)

        var classOrSuperClass: Class<*>? = clazz
        while (classOrSuperClass != null) {
            for (field in classOrSuperClass.declaredFields) {
                val fieldName = field.name
                if (QueryProConfig.final.shouldIgnoreFields().contains(fieldName)) {
                    continue
                }

                val idAnnotation: Id? = field.getAnnotation(Id::class.java)
                val columnAnnotation: Column? = field.getAnnotation(Column::class.java)
                val dbName = columnAnnotation?.name ?: to_snake_case(fieldName)
                if (idAnnotation != null) {
                    if (idColumn != null) {
                        throw UnSupportException("不支持联合主键")
                    }
                    idColumn = fieldName
                    idColumnType = field.type
                }
                if (fieldName == "id") {
                    idColumnMay = fieldName
                    idColumnType = field.type
                }

                val setterMethodName = "set${Character.toUpperCase(fieldName[0])}${fieldName.substring(1)}"
                val getterMethodName = "get${Character.toUpperCase(fieldName[0])}${fieldName.substring(1)}"

                val setter = try {
                    clazz.getMethod(setterMethodName, field.type)
                } catch (e: Exception) {
                    null
                }
                val getter = try {
                    clazz.getDeclaredMethod(getterMethodName)
                } catch (e: Exception) {
                    null
                } ?: try {
                    clazz.getMethod(getterMethodName)
                } catch (e: Exception) {
                    null
                }

                columns[dbName] = ParsedColumn(
                    javaName = fieldName,
                    javaType = field.type,
                    setter = { o, v ->
                        if (canAccess(field, o)) {
                            field.set(o, v)
                        } else {
                            if (setter == null) {
                                throw UnSupportException("无法访问私有且无setter的属性 {0}", fieldName)
                            }
                            setter.invoke(o, v)
                        }
                    },
                    getter = { o ->
                        if (o is Map<*, *>) {
                            return@ParsedColumn o[fieldName]
                        }
                        if (canAccess(field, o)) {
                            return@ParsedColumn field.get(o)
                        }
                        if (getter == null) {
                            throw UnSupportException("无法访问私有且无getter的属性 {0}", fieldName)
                        }
                        return@ParsedColumn getter.invoke(o)
                    },
                    dbName = dbName,
                )
            }
            classOrSuperClass = classOrSuperClass.superclass
        }

        ParsedClass(dbNameForTable, columns, idColumn ?: idColumnMay, idColumnType)
    }
}
