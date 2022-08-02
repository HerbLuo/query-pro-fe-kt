package cn.cloudself.query.config

import cn.cloudself.query.QueryStructureAction
import cn.cloudself.query.SKIP
import cn.cloudself.query.exception.ConfigException
import cn.cloudself.query.exception.IllegalImplements
import cn.cloudself.query.util.BeanProxy
import cn.cloudself.query.util.Result
import cn.cloudself.query.util.parseClass

@Suppress("unused")
class Lifecycle {
    internal val beforeExecTransformers = mutableListOf<QueryStructureTransformer>()
    internal val afterExecTransformers = mutableListOf<ResultTransformer>()

    abstract class BaseTransformersBuilder(private val action: QueryStructureAction) {
        private val transformers = mutableListOf<QueryStructureTransformer>()
        protected fun addTransformerWhenActionMatched(transformer: QueryStructureTransformer) = this.also {
            transformers.add { clazz, queryStructure, payload ->
                if (queryStructure.action == action) {
                    transformer(clazz, queryStructure, payload)
                } else {
                    Result.ok(queryStructure)
                }
            }
        }
        abstract fun addTransformer(transformer: QueryStructureTransformer): BaseTransformersBuilder
        fun build() = transformers
    }

    abstract class BaseResultTransformersBuilder(private val action: QueryStructureAction) {
        private val transformers = mutableListOf<ResultTransformer>()
        protected fun addTransformerWhenActionMatched(transformer: ResultTransformer) = this.also {
            transformers.add { result, queryStructure, payload ->
                if (queryStructure.action == action) {
                    transformer(result, queryStructure, payload)
                } else {
                    Result.ok(result)
                }
            }
        }
        abstract fun addTransformer(transformer: ResultTransformer): BaseResultTransformersBuilder
        fun build() = transformers
    }

    class BeforeInsertTransformersBuilder internal constructor(): BaseTransformersBuilder(QueryStructureAction.INSERT) {
        override fun addTransformer(transformer: QueryStructureTransformer) =
            this.also { addTransformerWhenActionMatched(transformer) }

        fun <T> addProperty(property: String, clazz: Class<T>, value: () -> T) = this.also {
            overrideProperty(property, clazz, value, false)
        }

        fun <T> overrideProperty(property: String, clazz: Class<*>, value: () -> T) = this.also {
            overrideProperty(property, clazz, value, true)
        }

        private fun <T> overrideProperty(property: String, clazz: Class<*>, getValue: () -> T, override: Boolean) {
            addTransformerWhenActionMatched { beanClass, queryStructure, _ ->
                val objs = queryStructure.insert?.data ?: throw IllegalImplements("insert 传入了空值。")
                @Suppress("UNCHECKED_CAST") val beanProxy = BeanProxy.fromClass(beanClass) as BeanProxy<Any, Any>
                for (obj in objs) {
                    val beanInstance = beanProxy.ofInstance(obj)
                    val column = beanInstance.getParsedClass().columns[property] ?: continue
                    if (clazz == column.javaType) {
                        if (column.getter(obj) == null || override) {
                            val value = getValue()
                            if (value != SKIP) {
                                column.setter(obj, value)
                            }
                        }
                    }
                }
                Result.ok(queryStructure)
            }
        }
    }

    class BeforeUpdateTransformersBuilder internal constructor(): BaseTransformersBuilder(QueryStructureAction.UPDATE) {
        override fun addTransformer(transformer: QueryStructureTransformer) =
            this.also { addTransformerWhenActionMatched(transformer) }

        fun <T> addProperty(property: String, value: () -> T) = this.also {
            overrideProperty(property, value, false)
        }

        fun <T> overrideProperty(property: String, value: () -> T) = this.also {
            overrideProperty(property, value, true)
        }

        private fun <T> overrideProperty(property: String, getValue: () -> T, override: Boolean) {
            addTransformerWhenActionMatched { clazz, queryStructure, _ ->
                @Suppress("UNCHECKED_CAST") val beanProxy = BeanProxy.fromClass(clazz) as BeanProxy<Any, Any>
                if (parseClass(clazz).columns[property] == null) {
                    return@addTransformerWhenActionMatched Result.ok(queryStructure)
                }
                val data = queryStructure.update?.data ?: throw IllegalImplements("update 传入了空值。")
                val beanInstance = beanProxy.ofInstance(data)
                val oldValue = beanInstance.getProperty(property)
                if (oldValue != null && !override) {
                    return@addTransformerWhenActionMatched Result.ok(queryStructure)
                }
                val value = getValue() ?: throw ConfigException("beforeUpdate.add(override)Property, 不能传入null值, 如需将值更新为null，使用QueryProConst(Kt).NULL")
                if (value != "skip") {
                    beanInstance.setProperty(property, value)
                }
                Result.ok(queryStructure)
            }
        }
    }

    class BeforeSelectTransformersBuilder internal constructor(): BaseTransformersBuilder(QueryStructureAction.SELECT) {
        override fun addTransformer(transformer: QueryStructureTransformer) =
            this.also { addTransformerWhenActionMatched(transformer) }
    }

    class BeforeDeleteTransformersBuilder internal constructor(): BaseTransformersBuilder(QueryStructureAction.DELETE) {
        override fun addTransformer(transformer: QueryStructureTransformer) =
            this.also { addTransformerWhenActionMatched(transformer) }
    }

    fun beforeSelect(builder: (builder: BeforeSelectTransformersBuilder) -> BeforeSelectTransformersBuilder) = this.also {
        beforeExecTransformers.addAll(builder(BeforeSelectTransformersBuilder()).build())
    }
    fun beforeUpdate(builder: (builder: BeforeUpdateTransformersBuilder) -> BeforeUpdateTransformersBuilder) = this.also {
        beforeExecTransformers.addAll(builder(BeforeUpdateTransformersBuilder()).build())
    }
    fun beforeDelete(builder: (builder: BeforeDeleteTransformersBuilder) -> BeforeDeleteTransformersBuilder) = this.also {
        beforeExecTransformers.addAll(builder(BeforeDeleteTransformersBuilder()).build())
    }
    fun beforeInsert(builder: (builder: BeforeInsertTransformersBuilder) -> BeforeInsertTransformersBuilder) = this.also {
        beforeExecTransformers.addAll(builder(BeforeInsertTransformersBuilder()).build())
    }
    fun beforeExec(transformer: QueryStructureTransformer) = this.also {
        beforeExecTransformers.add(transformer)
    }

    class AfterSelectTransformerBuilder internal constructor(): BaseResultTransformersBuilder(QueryStructureAction.SELECT) {
        override fun addTransformer(transformer: ResultTransformer) =
            this.also { addTransformerWhenActionMatched(transformer) }
    }

    class AfterUpdateTransformerBuilder internal constructor(): BaseResultTransformersBuilder(QueryStructureAction.UPDATE) {
        override fun addTransformer(transformer: ResultTransformer) =
            this.also { addTransformerWhenActionMatched(transformer) }
    }

    class AfterDeleteTransformerBuilder internal constructor(): BaseResultTransformersBuilder(QueryStructureAction.DELETE) {
        override fun addTransformer(transformer: ResultTransformer) =
            this.also { addTransformerWhenActionMatched(transformer) }
    }

    class AfterInsertTransformerBuilder internal constructor(): BaseResultTransformersBuilder(QueryStructureAction.INSERT) {
        override fun addTransformer(transformer: ResultTransformer) =
            this.also { addTransformerWhenActionMatched(transformer) }
    }

    fun afterSelect(builder: (builder: AfterSelectTransformerBuilder) -> AfterSelectTransformerBuilder) = this.also {
        afterExecTransformers.addAll(builder(AfterSelectTransformerBuilder()).build())
    }
    fun afterUpdate(builder: (builder: AfterUpdateTransformerBuilder) -> AfterUpdateTransformerBuilder) = this.also {
        afterExecTransformers.addAll(builder(AfterUpdateTransformerBuilder()).build())
    }
    fun afterDelete(builder: (builder: AfterDeleteTransformerBuilder) -> AfterDeleteTransformerBuilder) = this.also {
        afterExecTransformers.addAll(builder(AfterDeleteTransformerBuilder()).build())
    }
    fun afterInsert(builder: (builder: AfterInsertTransformerBuilder) -> AfterInsertTransformerBuilder) = this.also {
        afterExecTransformers.addAll(builder(AfterInsertTransformerBuilder()).build())
    }
    fun afterExec(transformer: ResultTransformer) = this.also {
        afterExecTransformers.add(transformer)
    }
}