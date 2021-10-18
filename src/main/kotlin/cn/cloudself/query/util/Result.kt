package cn.cloudself.query.util

import java.io.Serializable
import java.lang.Exception
import kotlin.jvm.JvmOverloads
import java.lang.RuntimeException
import java.lang.Void
import java.util.function.Supplier

class Result<T, E: Throwable> : Serializable {
    private var data: T? = null
    private var err: E? = null
    val isOk: Boolean
        get() = err == null

    fun data() = data
    fun err() = err

    fun <K> map(func: (T) -> K): Result<K, E> {
        val e = err
        @Suppress("UNCHECKED_CAST")
        return if (e != null) err(e) else ok(func(data as T))
    }

    fun and(func: (T) -> Result<T, E>): Result<T, E> {
        val e = err
        @Suppress("UNCHECKED_CAST")
        return if (e != null) err(e) else func(data as T)
    }

    fun <D> then(func: (T) -> Result<D, E>): Result<D, E> {
        val e = err
        @Suppress("UNCHECKED_CAST")
        return if (e != null) { err(e) } else func(data as T)
    }

    @JvmOverloads
    fun toMessage(defaultMsg: String = "nil"): String {
        if (err == null) {
            return defaultMsg
        }
        println("result 的返回结果是一个err")
        if (err is Exception) {
            (err as Exception).printStackTrace()
            val message = (err as Exception).message ?: return "null"
            return if (message.length < 100) {
                message
            } else "遇到一个错误：" + message.substring(0, 100)
        }
        return err.toString()
    }

    fun <FE: Throwable> mapErr(func: (E) -> FE): Result<T, FE> {
        @Suppress("UNCHECKED_CAST")
        return if (isOk) ok(data as T) else err(func(err as E))
    }

    fun unwrap(): T {
        if (isOk) {
            @Suppress("UNCHECKED_CAST")
            return data as T
        }
        if (err is RuntimeException) {
            throw (err as RuntimeException?)!!
        }
        throw RuntimeException(err.toString() + "")
    }

    inline fun getOrElse(func: (exception: E) -> T): T {
        if (isOk) {
            @Suppress("UNCHECKED_CAST")
            return this.data() as T
        }
        @Suppress("UNCHECKED_CAST")
        return func(this.err() as E)
    }

    companion object {
        private var logger = LogFactory.getLog(Result::class.java)
        @JvmStatic
        fun <FT, FE: Throwable> ok(data: FT): Result<FT, FE> {
            val result = Result<FT, FE>()
            result.data = data
            return result
        }

        @JvmStatic
        fun <FT, FE: Throwable> err(err: FE): Result<FT, FE> {
            val result = Result<FT, FE>()
            result.err = err
            return result
        }

        @JvmStatic
        fun <FT, FRes : Result<FT, Exception>> fromTryCatch(func: () -> FRes): Result<FT, Exception> {
            return try {
                logger.info("业务代码开始执行")
                val fRes = func()
                logger.info("业务代码执行完毕")
                fRes
            } catch (e: Exception) {
                logger.info("遇到一个错误" + e.message)
                err(e)
            }
        }

        @JvmStatic
        fun tryAll(funArr: List<Supplier<Result<Void?, Exception>>>): Result<Void?, Exception> {
            var result = ok<Void?, Exception>(null)
            for (`fun` in funArr) {
                try {
                    val res = `fun`.get()
                    if (!res.isOk) {
                        logger.info("返回了一个Result.err " + res.toMessage())
                        if (result.isOk) {
                            result = res
                        }
                    }
                } catch (e: Exception) {
                    logger.info("try catch到一个错误" + e.message)
                    if (result.isOk) {
                        result = err(e)
                    }
                }
            }
            return result
        }
    }
}