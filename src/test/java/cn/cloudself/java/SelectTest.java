package cn.cloudself.java;

import cn.cloudself.java.helpers.Helpers;
import cn.cloudself.java.helpers.query.Setting;
import cn.cloudself.java.helpers.query.SettingQueryPro;
import cn.cloudself.java.helpers.query.User;
import cn.cloudself.java.helpers.query.UserQueryPro;
import cn.cloudself.query.QueryProConfig;
import cn.cloudself.query.QueryProSql;
import org.intellij.lang.annotations.Language;
import org.junit.Test;

import java.util.List;

import static cn.cloudself.helpers.HelpersKt.*;

public class SelectTest {
    private void prepareData() {
        @Language("SQL")
        String sql = "TRUNCATE TABLE user;" +
                "INSERT INTO user (id, name, age) VALUES (1, 'hb', 18);" +
                "INSERT INTO user (id, name, age) VALUES (2, 'hb', 10);" +
                "INSERT INTO user (id, name, age) VALUES (3, 'herb', 18);" +
                "INSERT INTO user (id, name, age) VALUES (4, 'l', null);" +
                "TRUNCATE TABLE setting;" +
                "INSERT INTO setting (id, user_id, kee, value) VALUES (1, 1, 'lang', '简体中文');" +
                "INSERT INTO setting (id, user_id, kee, value) VALUES (2, 1, 'theme', 'dark');" +
                "INSERT INTO setting (id, user_id, kee, value) VALUES (3, 2, 'lang', '繁体中文');";
        QueryProSql.createBatchBySqlGroup(sql).update(Boolean.class);
    }

    @Test
    public void test() {
        initLogger();
        QueryProConfig.INSTANCE.setBeautifySql(false);
        QueryProConfig.INSTANCE.setLogicDelete(false);
        QueryProConfig.INSTANCE.setDataSource(getDataSource());

        prepareData();

        User user1 = new User().setId(1L).setName("hb").setAge(18);
        User user2 = new User().setId(2L).setName("hb").setAge(10);
        User user3 = new User().setId(3L).setName("herb").setAge(18);
        User user4 = new User().setId(4L).setName("l").setAge(null);

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ?", Helpers.listOf(1));
        final List<User> usersRun1 = UserQueryPro.selectBy().id().equalsTo(1).run();
        assertEqualsForJava(usersRun1, Helpers.listOf(user1));

        expectSqlResult("SELECT `setting`.`id` FROM `setting` WHERE `setting`.`id` = ?", Helpers.listOf(1));
        final List<Long> ids1 = SettingQueryPro.selectBy().id().equalsTo(1).columnLimiter().id();
        assertEqualsForJava(ids1, Helpers.listOf(1L));

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` = ? AND `user`.`age` = ?", Helpers.listOf("hb", 18));
        final List<User> usersRun2 = UserQueryPro.selectBy().name().is().equalsTo("hb").and().age().is().equalsTo(18).run();
        assertEqualsForJava(usersRun2, Helpers.listOf(user1));

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` in (?, ?)", Helpers.listOf("hb", "herb"));
        final List<User> usersRun3 = UserQueryPro.selectBy().name().in("hb", "herb").run();
        assertEqualsForJava(usersRun3, Helpers.listOf(user1, user2, user3));

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ? or `user`.`age` = ?", Helpers.listOf(1, 10));
        final List<User> usersRun4 = UserQueryPro
                .selectBy().id().is().equalsTo(1)
                .or().age().equalsTo(10)
                .run();
        assertEqualsForJava(usersRun4, Helpers.listOf(user1, user2));

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` <> ?", Helpers.listOf(2));
        final List<User> usersRun5 = UserQueryPro.selectBy().id().is().not().equalsTo(2).run();
        assertEqualsForJava(usersRun5, Helpers.listOf(user1, user3, user4));

        expectSqlResult("SELECT * FROM `user` WHERE UPPER(`user`.`name`) like UPPER(?)", Helpers.listOf("%H%"));
        final List<User> usersRun6 = UserQueryPro.selectBy().name().ignoreCase().like("%H%").run();
        assertEqualsForJava(usersRun6, Helpers.listOf(user1, user2, user3));

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`age` is null", Helpers.listOf());
        final List<User> usersRun7 = UserQueryPro.selectBy().age().is().nul().run();
        assertEqualsForJava(usersRun7, Helpers.listOf(user4));

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`id` = ? or (`user`.`age` = ? AND `user`.`name` like ?)", Helpers.listOf(1, 18, "%rb%"));
        final List<User> usersRun8 = UserQueryPro
                .selectBy().id().is().equalsTo(1)
                .or((it) -> it.age().equalsTo(18).and().name().like("%rb%"))
                .run();
        assertEqualsForJava(usersRun8, Helpers.listOf(user1, user3));

        expectSqlResult("SELECT * FROM `user` WHERE `user`.`name` like ? ORDER BY `user`.`id` DESC", Helpers.listOf("%h%"));
        final List<User> usersRun9 = UserQueryPro.selectBy().name().like("%h%").orderBy().id().desc().run();
        assertEqualsForJava(usersRun9, Helpers.listOf(user3, user2, user1));

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`id` DESC", Helpers.listOf());
        final List<User> usersRun10 = UserQueryPro.orderBy().id().desc().run();
        assertEqualsForJava(usersRun10, Helpers.listOf(user4, user3, user2, user1));

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`age` ASC, `user`.`id` DESC", Helpers.listOf());
        final List<User> usersRun11 = UserQueryPro.orderBy().age().asc().id().desc().run();
        assertEqualsForJava(usersRun11, Helpers.listOf(user4, user2, user3, user1));

        expectSqlResult("SELECT * FROM `user` ORDER BY `user`.`age` DESC, `user`.`id` ASC LIMIT 1", Helpers.listOf());
        final List<User> usersRun12 = UserQueryPro.orderBy().age().desc().id().asc().limit(1).run();
        assertEqualsForJava(usersRun12, Helpers.listOf(user1));

        expectSqlResult("SELECT * FROM `user` LIMIT 1", Helpers.listOf());
        final User userRun1 = UserQueryPro.selectBy().runLimit1();
        assertEqualsForJava(userRun1, user1);

        expectSqlResult("SELECT `user`.`id`, `user`.`age` FROM `user` WHERE `user`.`id` = ?", Helpers.listOf(1));
        final List<User> usersRun13 = UserQueryPro.selectBy().id().equalsTo(1).columnsLimiter().id().age().run();
        assertEqualsForJava(usersRun13, Helpers.listOf(new User().setId(user1.getId()).setName(null).setAge(user1.getAge())));

        expectSqlResult("SELECT `user`.`id`, `user`.`name` FROM `user` ORDER BY `user`.`age` DESC, `user`.`id` DESC LIMIT 1", Helpers.listOf());
        final List<User> usersRun14 = UserQueryPro
                .orderBy().age().desc().id().desc().limit(1)
                .columnsLimiter().id().name()
                .run();
        assertEqualsForJava(usersRun14, Helpers.listOf(new User().setId(user3.getId()).setName(user3.getName()).setAge(null)));

        expectSqlResult(
                "SELECT `setting`.`kee`, `setting`.`value` FROM `setting` LEFT JOIN `user` ON `setting`.`user_id` = `user`.`id` " +
                        "WHERE `setting`.`kee` = ? AND `setting`.`value` like ? AND UPPER(`user`.`name`) like UPPER(?) LIMIT 10",
                Helpers.listOf("lang", "%中文", "%H%"));
        final List<Setting> settingsRun1 = SettingQueryPro.EX // from setting
                .leftJoinOn(UserQueryPro.EX.joiner().id(), SettingQueryPro.EX.joiner().userId()) // left join user on user.id = setting.user_id
                .selectBy().kee().equalsTo("lang") // select ... where setting.kee = 'lang'
                .and().value().is().like("%中文") // and setting.value like '%中文'
                .andForeignField(UserQueryPro.EX.foreignField().name().ignoreCase().like("%H%")) // and upper(user.name) like upper("%luo%")
                .limit(10) // limit 10
                .columnsLimiter().kee().value() // select setting.kee, setting.value from setting ...
                .run();
        assertEqualsForJava(settingsRun1, Helpers.listOf(new Setting().setKee("lang").setValue("简体中文"), new Setting().setKee("lang").setValue("繁体中文")));
    }
}
