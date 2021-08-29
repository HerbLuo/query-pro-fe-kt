package cn.cloudself

import cn.cloudself.query.*
import org.junit.Test

class QueryProTest {
    @Suppress("UNUSED_VARIABLE")
    @Test
    fun test() {
        QueryProConfig.dryRun = true

        val ids = SettingQueryPro.selectBy().id.equalsTo(1).columnLimiter().id()

        UserQueryPro.selectBy().name.`is`.equalsTo(1).and().age.`is`.equalsTo(1000).run()

        UserQueryPro
            .selectBy().id.`is`.equalsTo(1)
            .or().name.`in`("Tom", "Cat", "Luo")
            .run()

        UserQueryPro.selectBy().id.`is`.not.equalsTo(2).run()

        UserQueryPro.selectBy().id.ignoreCase.like("%luo%").run()

        UserQueryPro.selectBy().id.`is`.nul().run()

        UserQueryPro
            .selectBy().id.`is`.equalsTo(1)
            .or { it.age.equalsTo(20).and().name.like("%Luo%") }
            .run()

        UserQueryPro.selectBy().name.like("%Luo%").orderBy().id().desc().run()

        UserQueryPro.orderBy().id().desc().run()

        UserQueryPro.orderBy().id().desc().name().asc().run()

        UserQueryPro.orderBy().id().desc().name().asc().limit(1).run()

        UserQueryPro
            .orderBy().id().desc().name().asc().limit(1)
            .columnsLimiter().id().name()
            .run()

        UserQueryPro
            .selectBy().id.equalsTo(1).columnsLimiter().id().age().run()

        val user = UserQueryPro.selectBy().runLimit1()

        SettingQueryProEx // from setting
            .leftJoinOn(UserQueryProEx.joiner().id(), SettingQueryProEx.joiner().userId()) // left join user on user.id = setting.user_id
            .selectBy().kee.equalsTo("autoStart") // select ... where setting.kee = 'autoStart'
            .and().value.equalsTo(true) // and setting.value = true
            .andForeignField(UserQueryProEx.foreignField().name.ignoreCase.like("%luo%")) // and upper(user.name) like upper("%luo%")
            .limit(10) // limit 10
            .columnsLimiter().kee().value() // select setting.kee, setting.value from setting ...
            .run()

    }
}
