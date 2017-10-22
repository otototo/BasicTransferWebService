package route

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern._
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.Config
import domain._
import route.request.{TransferRequest, TransferRequestFormat}
import route.response._
import service.storage.InMemoryStorage

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TransferRoute(config: Config, transferStorage: ActorRef)
                   (implicit val actorSystem: ActorSystem,
                                    materializer: Materializer)
  extends Directives
    with TransferRequestFormat
    with ResponseFormats {

  import TransferRoute._

  private implicit val timeout: Timeout = 3.seconds

  val route: Route = path(TransferPath) {
    post {
      entity(as[TransferRequest]) {
        transferRequest =>
          onComplete(transferStorage ? InMemoryStorage.Save(transferRequest.toDomain)) {
            case Success(_) =>
              complete(Message(StatusCodes.OK.intValue, "Transfer finished."))
            case Failure(e) =>
              complete(Message(StatusCodes.InternalServerError.intValue,
                "Transfer failed to due an unexpected error."))
          }
      }
    } ~ get {
      parameter(ToQuery) {
        to =>
          queryForTransfers(InMemoryStorage.LoadTransfersTo(Account(to)))
      } ~ parameter(FromQuery) {
        from =>
          queryForTransfers(InMemoryStorage.LoadTransfersFrom(Account(from)))
      }
    }
  }

  private def queryForTransfers(query: InMemoryStorage.StorageQuery): Route = {
    onComplete(transferStorage ? query) {
      case Success(records: TransferRecords) =>
        complete(ExecutedTransfers.fromDomain(records))
      case Failure(e) =>
        complete(Message(StatusCodes.InternalServerError.intValue,
          s"Transfer failed to due an unexpected error: ${e.getMessage}"))
    }
  }
}

private[route] object TransferRoute {
  val TransferPath = "transfer"
  val FromQuery = "from"
  val ToQuery = "to"
}