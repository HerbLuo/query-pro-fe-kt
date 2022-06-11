package cn.cloudself.query.util

import cn.cloudself.query.QueryProConfig

fun getCallInfo() =
    if (QueryProConfig.final.printCallByInfo()) {
        val stacks = Thread.currentThread().stackTrace
        var callByInfo = ""
        for (stack in stacks) {
            val className = stack.className
            val methodName = stack.methodName
            if (className.startsWith("cn.cloudself.query.") ||
                className.startsWith("java.lang.") ||
                className.endsWith("ColumnLimiterField") ||
                "selectByPrimaryKey" == methodName ||
                "deleteByPrimaryKey" == methodName
            ) {
                continue
            } else {
                callByInfo = "${className}.${methodName}(${stack.lineNumber})"
                break
            }
        }
        callByInfo
    } else {
        ""
    }
