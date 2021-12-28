package cn.cloudself.query.util

import java.lang.reflect.Field

val areBelowOfVersion8 = System.getProperty("java.version").startsWith("1.")

fun canAccess(field: Field, o: Any?): Boolean {
    return if (areBelowOfVersion8) {
        @Suppress("DEPRECATION") field.isAccessible
    } else {
        field.canAccess(o)
    }
}
