package server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import route.TransferRoute
import service.storage.InMemoryStorage

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ApplicationServer {
  val Url = "localhost"

  var bindingFuture: Future[Http.ServerBinding] =_

  /**Setup up Actor System, Routes and Configuration that are required to run the application. */
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val config: Config = ConfigFactory.load()

    val transferStorage: ActorRef = system.actorOf(Props[InMemoryStorage], "storage")
    start(new RouteComposition(new TransferRoute(config, transferStorage).route), config)
  }

  /**Start the Akka Http Server and serve provided routes. */
  def start(route : RouteComposition, config: Config)(implicit system: ActorSystem, materializer: ActorMaterializer): Unit = {
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val port = config.getInt("server.port")
    bindingFuture = Http().bindAndHandle(route.composition, Url, port)
    bindingFuture.onComplete{
      case Success(_) => println(s"Successfully started up the server on $Url:$port")
      case Failure(e) => println(s"Failed to start up the server duo to $e")
    }
  }
  /**Stop Akka Http Server and terminate actor system*/
  def stop()(implicit system: ActorSystem, executionContext: ExecutionContextExecutor): Unit = {
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate)
  }
}
