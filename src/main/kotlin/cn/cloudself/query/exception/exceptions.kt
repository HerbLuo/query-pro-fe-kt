package cn.cloudself.query.exception

import java.lang.Exception
import java.text.MessageFormat

class ConfigException: RuntimeException {
    constructor(message: String, vararg args: Any?): super(MessageFormat.format(message, *args))
    constructor(cause: Throwable, message: String, vararg args: Any?): super(MessageFormat.format(message, args), cause)
}

class UnSupportException: RuntimeException {
    constructor(message: String, vararg args: Any?): super(MessageFormat.format(message, *args))
    constructor(cause: Throwable, message: String, vararg args: Any?): super(MessageFormat.format(message, args), cause)
}

class IllegalImplements: RuntimeException {
    constructor(message: String, vararg args: Any?): super(MessageFormat.format(message, *args))
    constructor(cause: Throwable, message: String, vararg args: Any?): super(MessageFormat.format(message, args), cause)
}

class IllegalParameters: RuntimeException {
    constructor(message: String, vararg args: Any?): super(MessageFormat.format(message, *args))
    constructor(cause: Throwable, message: String, vararg args: Any?): super(MessageFormat.format(message, args), cause)
}

class MissingParameter: RuntimeException {
    constructor(message: String, vararg args: Any?): super(MessageFormat.format(message, *args))
    constructor(cause: Throwable, message: String, vararg args: Any?): super(MessageFormat.format(message, args), cause)
}

class IllegalCall: RuntimeException {
    constructor(message: String, vararg args: Any?): super(MessageFormat.format(message, *args))
    constructor(cause: Throwable, message: String, vararg args: Any?): super(MessageFormat.format(message, args), cause)
}
