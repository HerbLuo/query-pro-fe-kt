package cn.cloudself.query.exception

import java.lang.Exception
import java.text.MessageFormat

class ConfigException: Exception {
    constructor(message: String, vararg args: Any): super(MessageFormat.format(message, args))
    constructor(cause: Throwable, message: String, vararg args: Any): super(MessageFormat.format(message, args), cause)
}

class UnSupportException: Exception {
    constructor(message: String, vararg args: Any): super(MessageFormat.format(message, args))
    constructor(cause: Throwable, message: String, vararg args: Any): super(MessageFormat.format(message, args), cause)
}
