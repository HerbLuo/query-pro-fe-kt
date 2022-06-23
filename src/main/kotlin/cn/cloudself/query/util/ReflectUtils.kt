package cn.cloudself.query.util

import java.lang.reflect.Array
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter

val areBelowOfVersion8 = System.getProperty("java.version").startsWith("1.")

fun canAccess(field: Field, o: Any?): Boolean {
    return if (areBelowOfVersion8) {
        @Suppress("DEPRECATION") field.isAccessible
    } else {
        field.canAccess(o)
    }
}

class Reflect private constructor(
    private var obj: Any?,
    private val clazz: Class<*>? = null
) {
    companion object {
        @JvmStatic
        fun of(obj: Any?): Reflect {
            return Reflect(obj)
        }

        @JvmStatic
        fun of(clazz: Class<*>): Reflect {
            return Reflect(null, clazz)
        }
    }

    @Throws(Exception::class)
    fun invoke(methodName: String, vararg args: Any?): Reflect {
        obj = if (clazz != null) {
            getMethod(clazz, methodName, *args).invokePro(null, *args)
        } else {
            getMethod((obj ?: throw NullPointerException()).javaClass, methodName, *args).invokePro(obj, *args)
        }
        return this
    }

    fun getResult() = obj

    private fun Method.invokePro(obj: Any?, vararg args: Any?): Any? {
        val parameters = this.parameters
        var lastParameter: Parameter? = null
        val isVarArgs = if (parameters.isEmpty()) false else {
            lastParameter = parameters.last()
            lastParameter.isVarArgs
        }
        return if (isVarArgs && lastParameter != null) {
            val argList = args.toMutableList()
            val newArgs = argList.subList(0, parameters.size - 1)
            val varArgs = Array.newInstance(lastParameter.type.componentType, argList.size - parameters.size + 1)
            for ((j, i) in (parameters.size - 1 until argList.size).withIndex()) {
                Array.set(varArgs, j, argList[i])
            }
            newArgs.add(varArgs)
            this.invoke(obj, *newArgs.toTypedArray())
        } else {
            this.invoke(obj, *args)
        }
    }

    private fun getMethod(clazz: Class<*>, methodName: String, vararg args: Any?): Method {
        // 假设args是参数列表，查找对应的方法
        var findByMethodsMode = false
        val argsType = args.map { if (it == null) { findByMethodsMode = true; null } else it.javaClass }.toTypedArray()
        return if (findByMethodsMode)
            getMethodUseMethodsMode(clazz, methodName, *argsType)
        else
            try {
                clazz.getMethod(methodName, *argsType)
            } catch (e: NoSuchMethodException) {
                getMethodUseMethodsMode(clazz, methodName, *argsType)
            }
    }

    private fun getMethodUseMethodsMode(clazz: Class<*>, methodName: String, vararg args: Class<Any>?): Method {
        val methods = clazz.methods.filter { it.name == methodName }
        if (methods.isEmpty()) {
            throw NoSuchMethodException()
        }
        if (methods.size == 1) {
            return methods[0]
        }
        val filteredMethods = methods.filter {
            val argTypes = mutableListOf(*args)
            for (parameter in it.parameters) {
                if (parameter.isVarArgs) { // 肯定是最后一个参数了
                    val parameterType = parameter.type.componentType
                    for (argType in argTypes) {
                        if (!isArgMatchParam(argType, parameterType)) {
                            return@filter false
                        }
                    }
                    return@filter true
                }
                if (argTypes.isEmpty()) {
                    return@filter false
                }
                val argType = argTypes[0]
                argTypes.removeAt(0)
                if (!isArgMatchParam(argType, parameter.type)) {
                    return@filter false
                }
            }
            argTypes.isEmpty() // 空了，完全匹配了，则保留
        }
        if (filteredMethods.isEmpty()) {
            throw NoSuchMethodException("${clazz.name}.$methodName(${args.joinToString(", ")})")
        }
        if (filteredMethods.size == 1) {
            return filteredMethods[0]
        }
        return filteredMethods.find { !it.parameters.last().isVarArgs } ?: filteredMethods[0]
    }

    private fun isArgMatchParam(argType: Class<Any>?, parameterType: Class<*>): Boolean {
        if (argType == null) {
            return true
        }
        if (parameterType.isAssignableFrom(argType)) {
            return true
        }
        if (argType.isPrimitive || parameterType.isPrimitive) {
            if (argType.kotlin.javaPrimitiveType == parameterType.kotlin.javaPrimitiveType) {
                return true
            }
        }
        return false
    }
}
