package cn.cloudself.query.util

class Run<T> internal constructor(private val iterator: Iterator<T>, private val cb: (a: T) -> Unit) {
    fun run(concurrency: Int) {

        for (thread_i in 1 .. concurrency) {
            Thread {

//                this.cb()
            }
        }
    }
}

fun <T> Iterator<T>.forEachAsync(cb: (a: T) -> Unit) = Run(this, cb)
fun <T> Iterable<T>.forEachAsync(cb: (a: T) -> Unit) = Run(this.iterator(), cb)
fun <T> Array<T>.forEachAsync(cb: (a: T) -> Unit) = Run(this.iterator(), cb)
fun <K, V> Map<K, V>.forEachAsync(cb: (a: Map.Entry<K, V>) -> Unit) = Run(this.iterator(), cb)
