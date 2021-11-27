package route

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes, Uri}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import route.request.{TransferRequest, TransferRequestFormat}
import route.response.{ExecutedTransfers, ResponseFormats}
import server.RouteComposition
import service.storage.InMemoryStorage

import scala.concurrent.ExecutionContextExecutor

class TransferRoutesTest extends AnyFlatSpec
  with Matchers
  with ScalatestRouteTest
  with BeforeAndAfter
  with ResponseFormats
  with TransferRequestFormat {

  private lazy val testKit: ActorTestKit = ActorTestKit()

  implicit def typedSystem: ActorSystem[Nothing] = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.classicSystem

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val storageActor = testKit.spawn(InMemoryStorage())
  lazy val routes: RouteComposition = new RouteComposition(new TransferRoute(storageActor).route)

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