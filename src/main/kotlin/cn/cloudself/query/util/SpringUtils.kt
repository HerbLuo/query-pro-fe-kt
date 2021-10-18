package cn.cloudself.query.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class SpringUtils: ApplicationContextAware {

    companion object {
        private var applicationContext: ApplicationContext? = null
        private val logger = LogFactory.getLog(SpringUtils::class.java)

        @JvmStatic
        fun <T>getBean(beanType: Class<T>): T? {
            return try {
                applicationContext?.getBean(beanType)
            } catch (e: Exception) {
                logger.warn("获取bean失败: " + e.message)
                null
            }
        }

        @JvmStatic
        fun <T>getBean(name: String, clazz: Class<T>): T? {
            return try {
                applicationContext?.getBean(name, clazz)
            } catch (e: Exception) {
                logger.warn("获取bean失败: " + e.message)
                null
            }
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        SpringUtils.applicationContext = applicationContext
    }
}