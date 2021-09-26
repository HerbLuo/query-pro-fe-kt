/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * just from spring-jcl.
 */
package cn.cloudself.query.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.lang.ClassNotFoundException
import org.apache.logging.log4j.spi.ExtendedLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.spi.LocationAwareLogger
import java.io.Serializable
import kotlin.jvm.Volatile
import java.util.logging.LogRecord

interface Log {
    val isFatalEnabled: Boolean
    val isErrorEnabled: Boolean
    val isWarnEnabled: Boolean
    val isInfoEnabled: Boolean
    val isDebugEnabled: Boolean
    val isTraceEnabled: Boolean
    fun fatal(message: Any?)
    fun fatal(message: Any?, exception: Throwable?)
    fun error(message: Any?)
    fun error(message: Any?, exception: Throwable?)
    fun warn(message: Any?)
    fun warn(message: Any?, exception: Throwable?)
    fun info(message: Any?)
    fun info(message: Any?, exception: Throwable?)
    fun debug(message: Any?)
    fun debug(message: Any?, exception: Throwable?)
    fun trace(message: Any?)
    fun trace(message: Any?, exception: Throwable?)
}

object LogFactory {
    fun getLog(clazz: Class<*>) = getLog(clazz.name)
    @Suppress("MemberVisibilityCanBePrivate")
    fun getLog(name: String?) = LogAdapter.createLog(name)
}

internal object LogAdapter {
    private const val LOG4J_SPI = "org.apache.logging.log4j.spi.ExtendedLogger"
    private const val LOG4J_SLF4J_PROVIDER = "org.apache.logging.slf4j.SLF4JProvider"
    private const val SLF4J_SPI = "org.slf4j.spi.LocationAwareLogger"
    private const val SLF4J_API = "org.slf4j.Logger"
    private val logApi: LogApi

    /**
     * Create an actual [Log] instance for the selected API.
     * @param name the logger name
     */
    fun createLog(name: String?): Log {
        return when (logApi) {
            LogApi.LOG4J -> Log4jAdapter.createLog(name)
            LogApi.SLF4J_LAL -> Slf4jAdapter.createLocationAwareLog(name)
            LogApi.SLF4J -> Slf4jAdapter.createLog(name)
            else ->
                // Defensively use lazy-initializing adapter class here as well since the
                // java.logging module is not present by default on JDK 9. We are requiring
                // its presence if neither Log4j nor SLF4J is available; however, in the
                // case of Log4j or SLF4J, we are trying to prevent early initialization
                // of the JavaUtilLog adapter - e.g. by a JVM in debug mode - when eagerly
                // trying to parse the bytecode for all the cases of this switch clause.
                JavaUtilAdapter.createLog(name)
        }
    }

    private fun isPresent(className: String): Boolean {
        return try {
            Class.forName(className, false, LogAdapter::class.java.classLoader)
            true
        } catch (ex: ClassNotFoundException) {
            false
        }
    }

    private enum class LogApi {
        LOG4J, SLF4J_LAL, SLF4J, JUL
    }

    private object Log4jAdapter {
        fun createLog(name: String?): Log {
            return Log4jLog(name)
        }
    }

    private object Slf4jAdapter {
        fun createLocationAwareLog(name: String?): Log {
            val logger = LoggerFactory.getLogger(name)
            return if (logger is LocationAwareLogger) Slf4jLocationAwareLog(
                logger
            ) else Slf4jLog(logger)
        }

        fun createLog(name: String?): Log {
            return Slf4jLog(LoggerFactory.getLogger(name))
        }
    }

    private object JavaUtilAdapter {
        fun createLog(name: String?): Log {
            return JavaUtilLog(name)
        }
    }

    private class Log4jLog(name: String?) : Log, Serializable {
        private val logger: ExtendedLogger
        override val isFatalEnabled: Boolean
            get() = logger.isEnabled(Level.FATAL)
        override val isErrorEnabled: Boolean
            get() = logger.isEnabled(Level.ERROR)
        override val isWarnEnabled: Boolean
            get() = logger.isEnabled(Level.WARN)
        override val isInfoEnabled: Boolean
            get() = logger.isEnabled(Level.INFO)
        override val isDebugEnabled: Boolean
            get() = logger.isEnabled(Level.DEBUG)
        override val isTraceEnabled: Boolean
            get() = logger.isEnabled(Level.TRACE)

        override fun fatal(message: Any?) {
            log(Level.FATAL, message, null)
        }

        override fun fatal(message: Any?, exception: Throwable?) {
            log(Level.FATAL, message, exception)
        }

        override fun error(message: Any?) {
            log(Level.ERROR, message, null)
        }

        override fun error(message: Any?, exception: Throwable?) {
            log(Level.ERROR, message, exception)
        }

        override fun warn(message: Any?) {
            log(Level.WARN, message, null)
        }

        override fun warn(message: Any?, exception: Throwable?) {
            log(Level.WARN, message, exception)
        }

        override fun info(message: Any?) {
            log(Level.INFO, message, null)
        }

        override fun info(message: Any?, exception: Throwable?) {
            log(Level.INFO, message, exception)
        }

        override fun debug(message: Any?) {
            log(Level.DEBUG, message, null)
        }

        override fun debug(message: Any?, exception: Throwable?) {
            log(Level.DEBUG, message, exception)
        }

        override fun trace(message: Any?) {
            log(Level.TRACE, message, null)
        }

        override fun trace(message: Any?, exception: Throwable?) {
            log(Level.TRACE, message, exception)
        }

        private fun log(level: Level, message: Any?, exception: Throwable?) {
            if (message is String) {
                // Explicitly pass a String argument, avoiding Log4j's argument expansion
                // for message objects in case of "{}" sequences (SPR-16226)
                if (exception != null) {
                    logger.logIfEnabled(FQCN, level, null, message as String?, exception)
                } else {
                    logger.logIfEnabled(FQCN, level, null, message as String?)
                }
            } else {
                logger.logIfEnabled(FQCN, level, null, message, exception)
            }
        }

        companion object {
            private val FQCN = Log4jLog::class.java.name
            private val loggerContext = LogManager.getContext(
                Log4jLog::class.java.classLoader, false
            )
        }

        init {
            var context = loggerContext
            if (context == null) {
                // Circular call in early-init scenario -> static field not initialized yet
                context = LogManager.getContext(Log4jLog::class.java.classLoader, false)
            }
            logger = context!!.getLogger(name)
        }
    }

    private open class Slf4jLog<T : Logger?>(@Transient protected var logger: T) : Log, Serializable {
        protected val name: String = logger!!.name

        override val isFatalEnabled: Boolean
            get() = isErrorEnabled
        override val isErrorEnabled: Boolean
            get() = logger!!.isErrorEnabled
        override val isWarnEnabled: Boolean
            get() = logger!!.isWarnEnabled
        override val isInfoEnabled: Boolean
            get() = logger!!.isInfoEnabled
        override val isDebugEnabled: Boolean
            get() = logger!!.isDebugEnabled
        override val isTraceEnabled: Boolean
            get() = logger!!.isTraceEnabled

        override fun fatal(message: Any?) {
            error(message)
        }

        override fun fatal(message: Any?, exception: Throwable?) {
            error(message, exception)
        }

        override fun error(message: Any?) {
            if (message is String || logger!!.isErrorEnabled) {
                logger!!.error(message.toString())
            }
        }

        override fun error(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isErrorEnabled) {
                logger!!.error(message.toString(), exception)
            }
        }

        override fun warn(message: Any?) {
            if (message is String || logger!!.isWarnEnabled) {
                logger!!.warn(message.toString())
            }
        }

        override fun warn(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isWarnEnabled) {
                logger!!.warn(message.toString(), exception)
            }
        }

        override fun info(message: Any?) {
            if (message is String || logger!!.isInfoEnabled) {
                logger!!.info(message.toString())
            }
        }

        override fun info(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isInfoEnabled) {
                logger!!.info(message.toString(), exception)
            }
        }

        override fun debug(message: Any?) {
            if (message is String || logger!!.isDebugEnabled) {
                logger!!.debug(message.toString())
            }
        }

        override fun debug(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isDebugEnabled) {
                logger!!.debug(message.toString(), exception)
            }
        }

        override fun trace(message: Any?) {
            if (message is String || logger!!.isTraceEnabled) {
                logger!!.trace(message.toString())
            }
        }

        override fun trace(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isTraceEnabled) {
                logger!!.trace(message.toString(), exception)
            }
        }

        protected open fun readResolve(): Any {
            return Slf4jAdapter.createLog(name)
        }
    }

    private class Slf4jLocationAwareLog(logger: LocationAwareLogger) : Slf4jLog<LocationAwareLogger?>(logger),
        Serializable {
        override fun fatal(message: Any?) {
            error(message)
        }

        override fun fatal(message: Any?, exception: Throwable?) {
            error(message, exception)
        }

        override fun error(message: Any?) {
            if (message is String || logger!!.isErrorEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.ERROR_INT, message.toString(), null, null)
            }
        }

        override fun error(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isErrorEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.ERROR_INT, message.toString(), null, exception)
            }
        }

        override fun warn(message: Any?) {
            if (message is String || logger!!.isWarnEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.WARN_INT, message.toString(), null, null)
            }
        }

        override fun warn(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isWarnEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.WARN_INT, message.toString(), null, exception)
            }
        }

        override fun info(message: Any?) {
            if (message is String || logger!!.isInfoEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.INFO_INT, message.toString(), null, null)
            }
        }

        override fun info(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isInfoEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.INFO_INT, message.toString(), null, exception)
            }
        }

        override fun debug(message: Any?) {
            if (message is String || logger!!.isDebugEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.DEBUG_INT, message.toString(), null, null)
            }
        }

        override fun debug(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isDebugEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.DEBUG_INT, message.toString(), null, exception)
            }
        }

        override fun trace(message: Any?) {
            if (message is String || logger!!.isTraceEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.TRACE_INT, message.toString(), null, null)
            }
        }

        override fun trace(message: Any?, exception: Throwable?) {
            if (message is String || logger!!.isTraceEnabled) {
                logger!!.log(null, FQCN, LocationAwareLogger.TRACE_INT, message.toString(), null, exception)
            }
        }

        override fun readResolve(): Any {
            return Slf4jAdapter.createLocationAwareLog(name)
        }

        companion object {
            private val FQCN = Slf4jLocationAwareLog::class.java.name
        }
    }

    private class JavaUtilLog(private val name: String?) : Log, Serializable {
        @Transient
        private val logger: java.util.logging.Logger = java.util.logging.Logger.getLogger(name)

        override val isFatalEnabled: Boolean
            get() = isErrorEnabled
        override val isErrorEnabled: Boolean
            get() = logger.isLoggable(java.util.logging.Level.SEVERE)
        override val isWarnEnabled: Boolean
            get() = logger.isLoggable(java.util.logging.Level.WARNING)
        override val isInfoEnabled: Boolean
            get() = logger.isLoggable(java.util.logging.Level.INFO)
        override val isDebugEnabled: Boolean
            get() = logger.isLoggable(java.util.logging.Level.FINE)
        override val isTraceEnabled: Boolean
            get() = logger.isLoggable(java.util.logging.Level.FINEST)

        override fun fatal(message: Any?) {
            error(message)
        }

        override fun fatal(message: Any?, exception: Throwable?) {
            error(message, exception)
        }

        override fun error(message: Any?) {
            log(java.util.logging.Level.SEVERE, message, null)
        }

        override fun error(message: Any?, exception: Throwable?) {
            log(java.util.logging.Level.SEVERE, message, exception)
        }

        override fun warn(message: Any?) {
            log(java.util.logging.Level.WARNING, message, null)
        }

        override fun warn(message: Any?, exception: Throwable?) {
            log(java.util.logging.Level.WARNING, message, exception)
        }

        override fun info(message: Any?) {
            log(java.util.logging.Level.INFO, message, null)
        }

        override fun info(message: Any?, exception: Throwable?) {
            log(java.util.logging.Level.INFO, message, exception)
        }

        override fun debug(message: Any?) {
            log(java.util.logging.Level.FINE, message, null)
        }

        override fun debug(message: Any?, exception: Throwable?) {
            log(java.util.logging.Level.FINE, message, exception)
        }

        override fun trace(message: Any?) {
            log(java.util.logging.Level.FINEST, message, null)
        }

        override fun trace(message: Any?, exception: Throwable?) {
            log(java.util.logging.Level.FINEST, message, exception)
        }

        private fun log(level: java.util.logging.Level, message: Any?, exception: Throwable?) {
            if (logger.isLoggable(level)) {
                val rec: LogRecord
                if (message is LogRecord) {
                    rec = message
                } else {
                    rec = LocationResolvingLogRecord(level, message.toString())
                    rec.setLoggerName(name)
                    rec.setResourceBundleName(logger.resourceBundleName)
                    rec.setResourceBundle(logger.resourceBundle)
                    rec.setThrown(exception)
                }
                logger.log(rec)
            }
        }

        private fun readResolve(): Any {
            return JavaUtilLog(name)
        }
    }

    private class LocationResolvingLogRecord(level: java.util.logging.Level?, msg: String?) : LogRecord(level, msg) {
        @Volatile
        private var resolved = false
        override fun getSourceClassName(): String {
            if (!resolved) {
                resolve()
            }
            return super.getSourceClassName()
        }

        override fun setSourceClassName(sourceClassName: String) {
            super.setSourceClassName(sourceClassName)
            resolved = true
        }

        override fun getSourceMethodName(): String {
            if (!resolved) {
                resolve()
            }
            return super.getSourceMethodName()
        }

        override fun setSourceMethodName(sourceMethodName: String) {
            super.setSourceMethodName(sourceMethodName)
            resolved = true
        }

        private fun resolve() {
            val stack = Throwable().stackTrace
            var sourceClassName: String? = null
            var sourceMethodName: String? = null
            var found = false
            for (element in stack) {
                val className = element.className
                if (FQCN == className) {
                    found = true
                } else if (found) {
                    sourceClassName = className
                    sourceMethodName = element.methodName
                    break
                }
            }
            setSourceClassName(sourceClassName!!)
            setSourceMethodName(sourceMethodName!!)
        }

        // setMillis is deprecated in JDK 9
        private fun writeReplace(): Any {
            val serialized = LogRecord(level, message)
            serialized.loggerName = loggerName
            serialized.resourceBundle = resourceBundle
            serialized.resourceBundleName = resourceBundleName
            serialized.sourceClassName = sourceClassName
            serialized.sourceMethodName = sourceMethodName
            serialized.sequenceNumber = sequenceNumber
            serialized.parameters = parameters
            serialized.threadID = threadID
            @Suppress("DEPRECATION")
            serialized.millis = millis
            serialized.thrown = thrown
            return serialized
        }

        companion object {
            private val FQCN = JavaUtilLog::class.java.name
        }
    }

    init {
        when {
            isPresent(LOG4J_SPI) -> {
                logApi = if (isPresent(LOG4J_SLF4J_PROVIDER) && isPresent(SLF4J_SPI)) {
                    // log4j-to-slf4j bridge -> we'll rather go with the SLF4J SPI;
                    // however, we still prefer Log4j over the plain SLF4J API since
                    // the latter does not have location awareness support.
                    LogApi.SLF4J_LAL
                } else {
                    // Use Log4j 2.x directly, including location awareness support
                    LogApi.LOG4J
                }
            }
            isPresent(SLF4J_SPI) -> {
                // Full SLF4J SPI including location awareness support
                logApi = LogApi.SLF4J_LAL
            }
            isPresent(SLF4J_API) -> {
                // Minimal SLF4J API without location awareness support
                logApi = LogApi.SLF4J
            }
            else -> {
                // java.util.logging as default
                logApi = LogApi.JUL
            }
        }
    }
}
