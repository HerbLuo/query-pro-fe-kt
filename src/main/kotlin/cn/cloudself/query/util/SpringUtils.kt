package cn.cloudself.query.util

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class SpringUtils: ApplicationContextAware {
    companion object {
        private var applicationContext: ApplicationContext? = null

        fun <T>getBean(beanType: Class<T>): T? {
            return try {
                applicationContext?.getBean(beanType)
            } catch (e: Exception) {
                null
            }
        }

        fun <T>getBean(name: String, clazz: Class<T>): T? {
            return try {
                applicationContext?.getBean(name, clazz)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        SpringUtils.applicationContext = applicationContext
    }
}