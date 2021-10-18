package cn.cloudself.query.util

fun Class<*>.compatibleWithInt(): Boolean {
    return Int::class.java.isAssignableFrom(this) || Int::class.javaObjectType.isAssignableFrom(this)
}

fun Class<*>.compatibleWithBool(): Boolean {
    return Boolean::class.java.isAssignableFrom(this) || Boolean::class.javaObjectType.isAssignableFrom(this)
}
