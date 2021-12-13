package cn.cloudself.query.util

import java.util.*

class ListEx<T>(private val list: List<T>): List<T> by list {
    fun findAnyOpt() = Optional.ofNullable(list.getOrNull(0))
    fun findAnyOrNull() = list.getOrNull(0)
}
