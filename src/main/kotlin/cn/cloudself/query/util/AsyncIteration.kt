package cn.cloudself.query.util

class AsyncIteration<T> constructor(private val iterator: Iterator<T>, private val concurrency: Int = 2) {
    constructor(iterable: Iterable<T>, concurrency: Int = 2) : this(iterable.iterator(), concurrency)
    constructor(array: Array<T>, concurrency: Int = 2) : this(array.iterator(), concurrency)
    constructor(map: Map<*, *>, concurrency: Int = 2) : this(map.iterator() as Iterator<T>, concurrency)

}
