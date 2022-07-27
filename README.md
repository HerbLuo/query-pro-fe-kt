
### 例子
```java
// SELECT * FROM `user` WHERE `id` = ? LIMIT 1
User user = UserQueryPro.selectByPrimaryKey(1);

// SELECT * FROM `user` WHERE `user`.`id` = ?
List<User> users = UserQueryPro.selectBy().username().is().equalsTo("hello").run();

// SELECT * FROM `user` WHERE `user`.`id` = ? OR `user`.`age` = ?
List<User> users2 = UserQueryPro
        .selectBy().id().equalsTo(1) // is 是可选的
        .or().age().not().in(10, 11)
        .run();
```

### 简述

总的来说，`QueryPro`的运作流程分为两部分。

1. 在调用之前，`QueryPro`的所有代码都是为了合理的生成并修改`QueryStructure`这个结构，

2. 调用`run`之后，
通过`QueryStructureToSql`将`QueryStructure`就转成目标sql，
然后通过`JdbcQSR`执行sql并生成目标对象返回。

```
QueryStructure的设计哲学: 易于序列化, 以便多端生成，并传输
```

### 快速入门 & 文档

##### 1. 生成代码
```java

```

##### 2. 查询操作
```java
// 使用主键查询
// SELECT * FROM `user` WHERE `id` = ? LIMIT 1
User user0 = UserQueryPro.selectByPrimaryKey(1);

// 使用某个字段查询
// 所有的 is 是可以省略的，但有时候加着更好看
// SELECT * FROM `user` WHERE `user`.`id` = ?
List<User> users1_1 = UserQueryPro.selectBy().id().is().equalsTo(1).run();
List<User> users1_2 = UserQueryPro.selectBy().id().equalsTo(1).run();
List<User> users1_3 = UserQueryPro.selectBy().id(1).run();

// 使用in, between, lessThan, lessThanOrEqual, graterThan, graterThanOrEqual等查询
// SELECT * FROM `user` WHERE `user`.`name` in (?, ?)
List<User> users2_1 = UserQueryPro.selectBy().name().in("hb", "herb").run();
List<User> users2_2 = UserQueryPro.selectBy().name("hb", "herb").run();
// SELECT * FROM `user` WHERE `user`.`age` between (?, ?)
List<User> users2_3 = UserQueryPro.selectBy().age().between(18, 20).run();
// SELECT * FROM `user` WHERE `user`.`age` < ?
List<User> users2_5 = UserQueryPro.selectBy().age().lessThan(18).run();

// 使用and查询
// and是可以省略的，但有时候加着更好看
// SELECT * FROM `user` WHERE `user`.`name` = ? AND `user`.`age` = ?
List<User> usersRun3 = UserQueryPro
        .selectBy().name().is().equalsTo("hb")
        .and().age().is().equalsTo(18)
        .run();

// 使用or查询
// SELECT * FROM `user` WHERE `user`.`id` = ? OR `user`.`age` = ?
List<User> usersRun5_1 = UserQueryPro.selectBy().id().is().equalsTo(1).or().age().equalsTo(10).run();
// SELECT * FROM `user` WHERE `user`.`id` = ? OR (`user`.`age` = ? AND `user`.`name` like ?)
List<User> usersRun5_2 = UserQueryPro
        .selectBy().id().is().equalsTo(1)
        .or((it) -> it.age().equalsTo(18).and().name().like("%rb%"))
        .run();
// SELECT * FROM `user` WHERE `user`.`id` = ? OR (`user`.`age` = ? AND `user`.`name` like ?)
List<User> usersRun5_3 = UserQueryPro
        .selectBy().id().is().equalsTo(1)
        .or().parLeft().age().equalsTo(18).and().name().like("%rb%").parRight()
        .run();

// 使用not查询
// SELECT * FROM `user` WHERE `user`.`id` <> ?
List<User> usersRun6_1 = UserQueryPro.selectBy().id().is().not().equalsTo(2).run();
// SELECT * FROM `user` WHERE `user`.`id` not in (?, ?)
List<User> usersRun6_2 = UserQueryPro.selectBy().id().is().not().in(1, 2).run();
// SELECT * FROM `user` WHERE `user`.`id` not between (?, ?)
List<User> usersRun6_2 = UserQueryPro.selectBy().id().is().not().between(1, 2).run();
// SELECT * FROM `user` WHERE `user`.`name` not like ?
List<User> usersRun6_2 = UserQueryPro.selectBy().name().is().not().like("%H%").run();

// 忽略大小写
// SELECT * FROM `user` WHERE UPPER(`user`.`name`) like UPPER(?)
List<User> users7 = UserQueryPro.selectBy().name().ignoreCase().like("%H%").run();

// is null 查询
// SELECT * FROM `user` WHERE `user`.`age` is null
List<User> users8_1 = UserQueryPro.selectBy().age().is().nul().run();
// SELECT * FROM `user` WHERE `user`.`age` is not null
List<User> users8_2 = UserQueryPro.selectBy().age().is().not().nul().run();

// like查询
// SELECT * FROM `user` WHERE `user`.`name` like ? ORDER BY `user`.`id` DESC
List<User> users9 = UserQueryPro.selectBy().name().like("%h%").orderBy().id().desc().run();

// 排序
// SELECT * FROM `user` ORDER BY `user`.`id` DESC
List<User> users10_1 = UserQueryPro.orderBy().id().desc().run();
// SELECT * FROM `user` ORDER BY `user`.`age` ASC, `user`.`id` DESC
List<User> users10_2 = UserQueryPro.orderBy().age().asc().id().desc().run();

// 限制返回结果数量
// SELECT * FROM `user` ORDER BY `user`.`age` DESC, `user`.`id` ASC LIMIT 1
List<User> users11_1 = UserQueryPro.orderBy().age().desc().id().asc().limit(1).run();
// SELECT * FROM `user` LIMIT 1
User user11_2 = UserQueryPro.selectBy().runLimit1();

// 只需要返回部分字段
// SELECT `setting`.`id` FROM `setting` WHERE `setting`.`id` = ?
List<Long> ids12_1 = SettingQueryPro.selectBy().id().equalsTo(1).columnLimiter().id();
// SELECT `user`.`id`, `user`.`age` FROM `user` WHERE `user`.`id` = ?
List<User> users12_2 = UserQueryPro.selectBy().id().equalsTo(1).columnsLimiter().id().age().run();
// SELECT `user`.`id`, `user`.`name` FROM `user` ORDER BY `user`.`age` DESC, `user`.`id` DESC LIMIT 1
List<User> usersRun14 = UserQueryPro
        .orderBy().age().desc().id().desc().limit(1)
        .columnsLimiter().id().name()
        .run();

// take方法（方便写if等条件)
// SELECT * FROM `user` WHERE `user`.`id` = ? AND `user`.`name` = ?
List<User> usersRun15 = UserQueryPro
        .selectBy().id().equalsTo(1)
        .take(it -> True ? it.name().equalsTo("hb") : it.name().equalsTo("hb2"))
        .run();

// 自定义sql查询

```

##### 3. 插入操作
支持批量插入，当数据量大于20条时，会自动启用`BigMode`，  
`BigMode`下，当预估sql大于0.5M(实际一般在2M以内, 取决于非ascii字符的数量)或插入的行数大于1000，会自动分多次插入
```java
UserQueryPro.insert(...); // 参数支持 User, Map, Collection<User>, vararg User
```

##### 4. 更新操作
```java
UserQueryPro.updateSet(new User(19)).where.id.equalsTo(1).run()
UserQueryPro.updateSet().id(5).age(NULL).run()
```

##### 5. 删除操作
```java
// 逻辑删除默认启用，当存在deleted字段时，自动使用deleted字段进行逻辑删除
// UPDATE `setting` SET `deleted` = ? WHERE `setting`.`id` = ?
SettingQueryPro.deleteBy().id().is().equalsTo(1).run()

// SELECT * FROM `setting` WHERE  ( `setting`.`id` = ? OR `setting`.`kee` = ? )  AND `setting`.`deleted` = ? LIMIT 1
SettingQueryPro.selectBy().id().equalsTo(1).or().kee().equalsTo("lang").runLimit1()
```

##### 6. 直接执行sql
```java
QueryProSql.create().query()
QueryProSql.create().update()
QueryProSql.create().exec()
```

##### 7. 事务
`spring`环境下, 直接使用`@Transactional`即可。
非`spring`环境下，使用
```java
QueryProTransaction.use(() -> {
    // 业务代码
    return null;
})
```

##### 8. 多表关联支持
目前多表关联查询仍不够优雅，但能用
```java

```

### 配置
`QueryPro`的配置通过`QueryProConfig`进行。  
`QueryProConfig`的配置有如下五个作用域:  
`global`(全局), `request`(请求), `thread`(不推荐), `context`(代码块), `code`(private，针对某次查询)

##### 数据源
如果你使用了`Spring`, `QueryPro`会自动装载由`Spring`管理的`DataSource`。
当然你也可以手动指定它。
```java
DruidDataSource dataSource = new DruidDataSource();
dataSource.setUrl(url);
dataSource.setUsername(username);
dataSource.setPassword(password);
dataSource.setDriverClassName(drive);

// 针对本次请求，临时切换数据源
QueryPro.request.setDataSource(dataSource)
        
// 针对某段查询，临时切换数据源
QueryProConfig.context.use((config) -> {
    config.setDataSource(dataSource);

    User user = UserQueryPro.selectBy().name().equalsTo("username").runLimit1();
    List<Setting> themes = SettingQueryPro.selectBy().kee().equalsTo("theme").run();
});

// 针对某次查询，临时切换数据源
UserQueryPro.setDataSource(dataSource).selectBy().name().equalsTo("username").run();
```
##### 逻辑删除
逻辑删除默认就是启用行为。如果表字段存在`deleted`字段会使用逻辑删除。
```java
// 关闭逻辑删除
QueryProConfig.global.setLogicDelete(false); 
// 更改逻辑删除的字段，逻辑删除默认使用deleted字段，可以使用setLogicDeleteField更改
QueryProConfig.global.setLogicDeleteField("removed");
```
##### 日志信息
```java
QueryProConfig.global
    .setPrintSql(true) // 打印sql日志，默认开启
    .setBeautifySql(true) // 美化sql(加入空格，换行等)，默认开启
    .setPrintCallByInfo(true) // 打印调用QueryPro所在的代码行，默认开启
    .setPrintResult(true); // 打印返回结果，默认开启
```
##### 设置需要忽略字段
例如，忽略`serialVersionUID`这个字段 (这是默认行为)
```java
QueryProConfig.global.shouldIgnoreFields().add("serialVersionUID");
```
##### 自定义QueryStructure解析器
`QueryPro`在处理`QueryStructure`转返回结果时，默认实现并使用了`JdbcQSR`，  
它使用Jdbc进行查询，不依赖`Mybatis`, `Spring Data`等框架。  
出于某些目的，你也可以替换它。

```java
QueryProConfig.global.setQueryStructureResolver(new JdbcQSR());
```
##### 处理返回结果

###### 1. 指定返回结果的类型  

`JdbcQSR`会解析需返回结果的类型，
并尝试将`JDBC`的返回结果`ResultSet`转换成目标类型。  

但是当无法解析出具体的字段类型时，例如：`runAsMap`方法，返回一个`Map`，此时便无法知晓需转换的具体类型是什么，  
这时`JdbcQSR`会使用`JDBC`自带的`getObject`将返回结果转换成java类型，其行为参考下表，具体可至
`com.mysql.cj.jdbc.result.ResultSetImpl.getObject`查看

| SQL Type           | Java Type            |
|--------------------|----------------------|
| BIT                | byte[], deserialized |
| BOOLEAN            | Boolean              |
| TINYINT            | Integer              |
| TINYINT_UNSIGNED   | Integer              |
| SMALLINT           | Integer              |
| SMALLINT_UNSIGNED  | Integer              |
| MEDIUMINT          | Integer              |
| MEDIUMINT_UNSIGNED | Integer              |
| INT                | Integer              |
| INT_UNSIGNED       | Long                 |
| BIGINT             | Long                 |
| BIGINT_UNSIGNED    | BigInteger           |
| DECIMAL            | BigDecimal           |
| DECIMAL_UNSIGNED   | BigDecimal           |
| FLOAT              | Float                |
| FLOAT_UNSIGNED     | Float                |
| DOUBLE             | Double               |
| DOUBLE_UNSIGNED    | Double               |
| CHAR               | String               |
| ENUM               | String               |
| SET                | String               |
| VARCHAR            | String               |
| TINYTEXT           | String               |
| TEXT               | String               |
| MEDIUMTEXT         | String               |
| LONGTEXT           | String               |
| JSON               | String               |
| GEOMETRY           | byte[]               |
| BINARY             | byte[], deserialized |
| VARBINARY          | byte[], deserialized |
| TINYBLOB           | byte[], deserialized |
| MEDIUMBLOB         | byte[], deserialized |
| LONGBLOB           | byte[], deserialized |
| BLOB               | byte[], deserialized |
| YEAR               | Date, Short          |
| DATE               | Date                 |
| TIME               | Time                 |
| TIMESTAMP          | Timestamp            |
| DATETIME           | LocalDateTime        |
| -                  | String               |

可以看到它会将无符号的长整型`BIGINT_UNSIGNED`类型转为`BigInteger`。  
但是针对`id`字段，通常我们希望转为`Long`类型， 
就可以使用如下代码配置一个dbColumn解析器。
```java
// 如果没有指定返回结果的类型，将id或_id结尾的无符号BIGINT类型转为Long。(这是默认行为，无需额外配置)
QueryProConfig.global.dbColumnInfoToJavaType().put(
        (columnInfo) -> {
            if (columnInfo.getType().startsWith("BIGINT")) { // 以BIGINT开头，包括了BIGINT_UNSIGNED
                String label = columnInfo.getLabel();
                if (label.equals("id") || label.endsWith("_id")) { // 字段名为id或者以_id结尾的
                    return true;
                }
            }
            return false;
        },
        Long.class // 转为Long类型
);
```

###### 2.增加某种返回类型的支持

上面说到：`JdbcQSR`会解析需返回结果的类型，  
并尝试将`JDBC`的返回结果`ResultSet`转换成目标类型。  

这是通过配置中的`ResultSetParsers`实现的，我们可以通过`QueryProConfig.global.addResultSetParser()`添加它。
默认支持的类型有`BigDecimal`, `Byte`, `ByteArray`, `Date`, `LocalDate`, 
`LocalTime`, `LocalDateTime`, `java.sql.Date`, `Double`, `Float`, `Int`,
`Long`, `Time`, `Timestamp`, `Short`, `String` 以及 `枚举类型`。

```java
// 例如，这两个ResultSetParser一个简单一个略复杂，都已经内置在了`QueryPro`中
QueryProConfig.global
        .addResultSetParser(
            LocalDateTime.class,
            (resultSet, columIndex) -> resultSet.getTimestamp(columIndex).toLocalDateTime()
        )
        .addResultSetParserEx((resultSet, targetClass, columnIndex) -> { // 当需要同时支持多种返回类型时，可以使用addResultSetParserEx
            if (!targetClass.isEnum()) {
                return Optional.empty();
            }
            return Optional.of(Enum.valueOf((Class) targetClass, resultSet.getString(columnIndex)));
        });
```

##### setParam

##### lifecycle

### 后续规划
类似这样的语句的处理 UPDATE word SET score = score + 1 WHERE id = 1
对sum, concat, group_concat, discount等 的支持
QueryStructure 不复制
添加带下划线驼峰式的列（更新操作）
