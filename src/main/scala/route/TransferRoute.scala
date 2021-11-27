package route

import akka.Done
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import domain._
import route.request.{TransferRequest, TransferRequestFormat}
import route.response._
import service.storage.InMemoryStorage
import service.storage.InMemoryStorage.{LoadTransfersFrom, LoadTransfersTo, Save}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TransferRoute(transferStorage: ActorRef[InMemoryStorage.Command])
                   (implicit val actorSystem: ActorSystem[_])
  extends Directives
    with TransferRequestFormat
    with ResponseFormats {

  import TransferRoute._

  private implicit val timeout: Timeout = 3.seconds

  val route: Route = path(TransferPath) {
    post {
      entity(as[TransferRequest]) {
        transferRequest =>
          handleEventualDone(saveTransfer(transferRequest))
      }
    } ~ get {
      parameter(ToQuery) {
        to =>
          handleEventualTransfer(transferTo(to))
      } ~ parameter(FromQuery) {
        from =>
          handleEventualTransfer(transferFrom(from))
      }
    }
  }

  private def handleEventualDone(eventualDone: Future[Done]) = {
    onComplete(eventualDone) {
      case Success(_) =>
        complete(Message(StatusCodes.OK.intValue, "Transfer finished."))
      case Failure(e) =>
        handleFailure(e)
    }
  }

  private def handleEventualTransfer(eventualRecords: Future[TransferRecords]) = {
    onComplete(eventualRecords) {
      case Success(records: TransferRecords) =>
        complete(ExecutedTransfers.fromDomain(records))
      case Failure(e) =>
        handleFailure(e)
    }
  }

  private def handleFailure(e: Throwable) = {
    complete(Message(StatusCodes.InternalServerError.intValue,
      s"Transfer failed to due an unexpected error: ${e.getMessage}"))
  }

  private def transferFrom(from: String) = {
    transferStorage ? (LoadTransfersFrom(Account(from), _))
  }

  private def transferTo(to: String) = {
    transferStorage ? (LoadTransfersTo(Account(to), _))
  }

  private def saveTransfer(transferRequest: TransferRequest): Future[Done] = {
    transferStorage ? (Save(transferRequest.toDomain, _))
  }
}

private[route] object TransferRoute {
  val TransferPath = "transfer"
  val FromQuery = "from"
  val ToQuery = "to"
}