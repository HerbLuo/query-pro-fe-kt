package cn.cloudself.query.structure_reolsver

import cn.cloudself.query.QueryProConfig
import cn.cloudself.query.exception.UnSupportException
import javax.persistence.Column
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

data class ParsedColumn(
    val javaName: String,
    val javaType: Class<*>,
    val setter: (o: Any?, v: Any?) -> Unit,
    val dbName: String,
)

class Ref<R>(var value: R)

val beanProxyCaches = mutableMapOf<Class<*>, BeanProxy<*, *>>()

/**
 * Bean代理，
 * 支持生成三种类型的数据: Map, 基本对象, JavaBean
 *
 * 使用[BeanProxy.fromClass]构造该对象
 * 使用[BeanProxy.newInstance]创建临时对象[BeanInstance]
 * 使用[BeanInstance.setProperty]设置目标对象的属性
 * 使用[BeanInstance.getJavaType]获取目标某字段的类型
 * 使用[BeanInstance.toResult]转为目标对象
 */
class BeanProxy<T, R>(
    /**
     * 创建目标对象或临时对象
     */
    private val createInstance: () -> T,
    /**
     * 设置属性
     */
    private val setProperty: (o: T, p: String, v: Any?) -> Unit,
    /**
     * 获取某属性的类型
     */
    private val getJavaType: (p: String) -> Class<*>?,
) {
    /**
     * 临时对象转为目标对象
     */
    private var toResult: (o: T) -> R = {
        @Suppress("UNCHECKED_CAST")
        it as R
    }
    private fun setToResult(toResult: (o: T) -> R) = this.also { this.toResult = toResult }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <R> fromClass(clazz: Class<R>): BeanProxy<*, R> {
            val cachedBeanProxy = beanProxyCaches[clazz]
            if (cachedBeanProxy != null) {
                return cachedBeanProxy as BeanProxy<*, R>
            }

            val beanProxy = when {
                // 需要的类型是一个map
                Map::class.java.isAssignableFrom(clazz) -> {
                    val createInstance = when {
                        LinkedHashMap::class.java.isAssignableFrom(clazz) -> { { LinkedHashMap<String, Any?>() as R } }
                        HashMap::class.java.isAssignableFrom(clazz) -> { { HashMap<String, Any?>() as R } }
                        else -> { throw UnSupportException("不支持LinkedHashMap与HashMap<String, Object>以外的Map") }
                    }
                    BeanProxy(
                        createInstance,
                        { result, property, value -> (result as MutableMap<String, Any?>)[property] = value },
                        { null }
                    )
                }
                // 需要的类型是一个数据库基本类型(Long, Int, Date等)
                QueryProConfig.getSupportedColumnType().any { it.isAssignableFrom(clazz) } -> {
                    BeanProxy<Ref<R?>, R>(
                        { Ref(null)  },
                        { o, _, v -> o.value = v as R },
                        { clazz },
                    ).setToResult { it.value as R }
                }
                // 需要返回的是一个JavaBean
                else -> {
                    try {
                        val noArgConstructor = clazz.getDeclaredConstructor()

                        val columns = mutableMapOf<String, ParsedColumn>()
                        var classOrSuperClass: Class<*>? = clazz
                        while (classOrSuperClass != null) {
                            for (field in classOrSuperClass.declaredFields) {
                                val fieldName = field.name
                                val columnAnnotation: Column? = field.getAnnotation(Column::class.java)
                                val dbName = columnAnnotation?.name ?: fieldName.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()

                                val setter = clazz.getMethod("set${fieldName[0].uppercaseChar()}${fieldName.substring(1)}", field.type)

                                columns[dbName] = ParsedColumn(
                                    javaName = fieldName,
                                    javaType = field.type,
                                    setter = { o, v ->
                                        setter.invoke(o, v)
                                    },
                                    dbName = dbName
                                )
                            }
                            classOrSuperClass = classOrSuperClass.superclass
                        }

                        BeanProxy<R, R>(
                            { noArgConstructor.newInstance() },
                            { r, p, v ->
                                val column = columns[p]
                                if (column == null) {
                                    println("[WARN] 数据库返回了多余的数据 $p")
                                    return@BeanProxy
                                }
                                column.setter(r, v)
                            },
                            { p -> columns[p]?.javaType }
                        )
                    } catch (e: Exception) {
                        throw UnSupportException(e, "{0} 没有找到无参构造函数，该类是一个JavaBean吗, " +
                                "对于Kotlin，需使用kotlin-maven-noarg生成默认的无参构造函数" +
                                "或在生成工具中配置QueryProFileMaker.disableKtNoArgMode()来生成默认的构造函数", clazz)
                    }
                }
            }

            beanProxyCaches[clazz] = beanProxy
            return beanProxy
        }
    }

    class BeanInstance<T, R>(
        private val instance: T,
        private val proxy: BeanProxy<T, R>,
    ) {
        fun setProperty(p: String, v: Any?) = this.also { proxy.setProperty(instance, p, v) }
        fun getJavaType(p: String) = proxy.getJavaType(p)
        fun toResult(): R = proxy.toResult(instance)
    }
    fun newInstance() = BeanInstance(createInstance(), this)
}
