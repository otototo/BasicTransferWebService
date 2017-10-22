package route

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes, Uri}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.pattern.gracefulStop
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import route.request.{TransferRequest, TransferRequestFormat}
import route.response.{ExecutedTransfers, ResponseFormats}
import server.RouteComposition
import service.storage.InMemoryStorage

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor}

class TransferRoutesTest extends FlatSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfter
  with ResponseFormats
  with TransferRequestFormat {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val config: Config = ConfigFactory.load()
  private var storageActor: ActorRef = _
  private var routes: RouteComposition = _

  before {
    storageActor = system.actorOf(Props[InMemoryStorage], "storage")
    routes = new RouteComposition(
      new TransferRoute(config: Config, storageActor).route)
  }

  after {
    Await.ready(gracefulStop(storageActor, 3.seconds), 3.seconds)
  }

  private def httpEntity(transfer: TransferRequest) =
    HttpEntity(ContentTypes.`application/json`, transferRequestFormat.write(transfer).toString())

  "Executed transfer" should "be retrieved by searching by source account" in {
    val request = TransferRequest("123456", "678912", 55)
    Post("/" + TransferRoute.TransferPath, httpEntity(request)) ~> routes.composition ~> check {
      status should be(StatusCodes.OK)
    }
    val getQuery = Uri("/" + TransferRoute.TransferPath)
      .withQuery(Uri.Query((TransferRoute.FromQuery, request.sourceAccount)))
    Get(getQuery) ~> routes.composition ~> check {
      then_response_should_have_transfer_record(request)
    }
  }

  "Executed transfer" should "be retrieved by searching by destination account" in {
    val request = TransferRequest("123456", "678912", 55)
    Post("/" + TransferRoute.TransferPath, httpEntity(request)) ~> routes.composition ~> check {
      status should be(StatusCodes.OK)
    }
    val getQuery = Uri("/" + TransferRoute.TransferPath)
      .withQuery(Uri.Query((TransferRoute.ToQuery, request.destinationAccount)))
    Get(getQuery) ~> routes.composition ~> check {
      then_response_should_have_transfer_record(request)
    }
  }


  private def then_response_should_have_transfer_record(request: TransferRequest) = {
    status should be(StatusCodes.OK)
    val transferHistory = entityAs[ExecutedTransfers].transfers
    transferHistory should have size 1
    val transfer = transferHistory.head
    transfer.amount should be(request.amount)
    transfer.sourceAccount should be(request.sourceAccount)
    transfer.destinationAccount should be(request.destinationAccount)
  }
}