package server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import route.TransferRoute
import service.storage.InMemoryStorage

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ApplicationServer {
  val Url = "localhost"
  var bindingFuture: Future[Http.ServerBinding] = _

  /** Setup up Actor System, Routes and Configuration that are required to run the application. */
  def main(args: Array[String]): Unit = {
    //    val transferStorage: ActorRef = system.actorOf(Props[InMemoryStorage], "storage")
    //    val routes = new TransferRoute(config, transferStorage)
    //    val composition = new RouteComposition(route.route)
    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val transferStorage = context.spawn(InMemoryStorage(), "InMemoryStorageActor")
      context.watch(transferStorage)

      val routes: TransferRoute = new TransferRoute(transferStorage)(context.system)
      val routeComposition = new RouteComposition(routes.route)
      startHttpServer(routeComposition)(context.system)

      Behaviors.empty
    }

    val system = ActorSystem[Nothing](rootBehavior, "TransferWebServer")
  }

  /** Start the Akka Http Server and serve provided routes. */
  def startHttpServer(routes: RouteComposition)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes.composition)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(exception) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", exception)
        system.terminate();
    }
  }

  /** Stop Akka Http Server and terminate actor system */
  def stop()(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate)
  }
}
