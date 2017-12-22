package movierating

import io.vertx.core.http.HttpServer
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.ResultSet
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.UpdateResult
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.core.json.*
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch

class AppMySQL : CoroutineVerticle() {

  private lateinit var client : JDBCClient

  suspend override fun start() {

    client = JDBCClient.createShared(vertx, json {
        /*obj(
          "url" to "jdbc:hsqldb:mem:test?shutdown=true",
          "driver_class" to "org.hsqldb.jdbcDriver",
          "max_pool_size-loop" to 30
        )*/
        obj(
                "url" to "jdbc:mysql://localhost:3306/kotlin?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull",
                "user" to "root",
                "password" to "buzhidao",
                "driver_class" to "com.mysql.jdbc.Driver"
        )
    })

    // Build Vert.x Web router
    val router = Router.router(vertx)
    router.get("/movie/:id").coroutineHandlerMySQL { ctx -> getMovie(ctx) }
    router.post("/rateMovie/:id").coroutineHandlerMySQL { ctx -> rateMovie(ctx) }
    router.get("/getRating/:id").coroutineHandlerMySQL { ctx -> getRating(ctx) }
    router.route("/getSlow/:isSlow").coroutineHandlerMySQL { ctx -> getSlow(ctx) }

    // Start the server
      awaitResult<HttpServer> {
          vertx.createHttpServer()
                  .requestHandler(router::accept)
                  .listen(config.getInteger("http.port", 8088), it)
      }
  }

  // Send info about a movie
  suspend fun getMovie(ctx: RoutingContext) {
    val id = ctx.pathParam("id")
    val result = awaitResult<ResultSet> { client.queryWithParams("SELECT TITLE FROM V_MOVIE WHERE ID=?", json { array(id) }, it) }
    if (result.rows.size == 1) {
      ctx.response().putHeader("content-type", "application/json").end(json {
          obj("id" to id, "title" to result.rows[0]["TITLE"]).encode()
      })
    } else {
      ctx.response().setStatusCode(404).end()
    }
  }

  // Rate a movie
  suspend fun rateMovie(ctx: RoutingContext) {
    val movie = ctx.pathParam("id")
    val sec = ctx.pathParam("sec")
    val rating = Integer.parseInt(ctx.queryParam("getRating")[0])
    val connection = awaitResult<SQLConnection> { client.getConnection(it) }
    connection.use {
      val result = awaitResult<ResultSet> { connection.queryWithParams("SELECT TITLE FROM V_MOVIE WHERE ID=?", json { array(movie) }, it) }
      if (result.rows.size == 1) {
          awaitResult<UpdateResult> { connection.updateWithParams("INSERT INTO V_RATING (ID,VALUE, MOVIE_ID) VALUES ?, ?", json { array(null, rating, movie) }, it) }
        ctx.response().setStatusCode(200).end("rate ok")
      } else {
        ctx.response().setStatusCode(404).end("rate fail")
      }
    }
  }

    /**
     * 这里获得是平均值 改大一点投票值 才可以
     */
  suspend fun getRating(ctx: RoutingContext) {
    val id = ctx.pathParam("id")
    val result = awaitResult<ResultSet> { client.queryWithParams("SELECT AVG(VALUE) AS VALUE FROM V_RATING WHERE MOVIE_ID=?", json { array(id) }, it) }
    ctx.response().putHeader("content-type", "application/json").end(json {
        obj("id" to id, "getRating" to result.rows[0]["VALUE"]).encode()
    })
  }
    /**
     * 慢查询是否卡顿
     * 打开两个浏览器 发现后发送的快查询 还是会等待 之前的慢查询,并没有非阻塞的效果
     */
  suspend fun getSlow(ctx: RoutingContext) {
    val id = ctx.pathParam("isSlow")
        if (id.equals("true")){
            //全表扫描
            var result = awaitResult<ResultSet> { client.query("SELECT name from slow ORDER BY name limit 0,100000", it) }
            ctx.response().putHeader("content-type", "application/json").end(json {
                array(result).encode()
            })
        }else{
            //分页
            var result = awaitResult<ResultSet> { client.query("SELECT * from slow limit 0,10", it) }
            ctx.response().putHeader("content-type", "application/json").end(json {
                array(result).encode()
            })
        }


  }
}

/**
 * An extension method for simplifying coroutines usage with Vert.x Web routers
 */
fun Route.coroutineHandlerMySQL(fn : suspend (RoutingContext) -> Unit) {
  handler { ctx ->
      launch(ctx.vertx().dispatcher()) {
          try {
              fn(ctx)
          } catch (e: Exception) {
              ctx.fail(e)
          }
      }
  }
}