package cn.cloudself.query.util

@Suppress("FunctionName")
fun onlyAZaz_(str: String): String {
    return str.map {
        when (it) {
            in 'a'..'z' -> it
            in 'A' .. 'Z' -> it
            else -> '_'
        }
    }.joinToString("")
}

fun main() {
    println(onlyAZaz_("aAaMaa_%+aaa_a*_b"))
}
