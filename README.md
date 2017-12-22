# Vert.x Kotlin 官方协程样例

[vertx and kotlin](https://github.com/vert-x3/vertx-examples/tree/master/kotlin-examples/coroutines) 原项目地址,由于自己要修改代码所以单独建立了一个仓库

[kotlin 协程官方文档](http://kotlinlang.org/docs/reference/coroutines.html#generators-api-in-kotlincoroutines)
[kotlin 协程设计文档](https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md)

一个电影投票的Rest应用,示例了 [Kotlin](https://kotlinlang.org/) 协程如何在Vert.x中使用

用协程可以以同步的方式写异步代码,功能 `投票电影` 是一个最好的例子显示两个SQL客户端操作被包裹`use`代码块,管理SQL连接

## 在IDE运行

启动main.kt函数

## 作为一个far jar 运行

```
> mvn package
> java -jar target/kotlin-coroutines-examples.jar
```


## API

应用导出了REST API投票电影:

你可以获得一个电影的更多信息

```
> curl http://localhost:8088/movie/starwars
{"id":"starwars","title":"Star Wars"}
```

你可以获得一个电影的当前投票情况(夺宝奇兵):

```
> curl http://localhost:8088/getRating/indianajones
{"id":"indianajones","getRating":5}
```

最终你可以给一部电影投票(星战)

```
> curl -X POST http://localhost:8088/rateMovie/starwars?getRating=4
```
