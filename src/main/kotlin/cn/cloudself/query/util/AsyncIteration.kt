package cn.cloudself.query.util

class AsyncIteration<T> constructor(private val concurrency: Int = 2) {

    fun of() {
    }

}

fun Iterator<*>.forEachAsync() {


}

fun Iterable<*>.forEachAsync() = this.iterator().forEachAsync()
fun Array<*>.forEachAsync() = this.iterator().forEachAsync()
fun Map<*, *>.forEachAsync() = this.iterator().forEachAsync()
