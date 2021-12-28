package cn.cloudself.query.util

import java.lang.reflect.Field

val areBelowOfVersion8 = Runtime::class.java.`package`.implementationVersion.startsWith("1.")

fun canAccess(field: Field, o: Any?): Boolean {
    return if (areBelowOfVersion8) {
        @Suppress("DEPRECATION") field.isAccessible
    } else {
        field.canAccess(o)
    }
}
