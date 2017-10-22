package route.response

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ResponseFormats extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat2(Message)
  implicit val executedTransferFormat: RootJsonFormat[ExecutedTransfer] = jsonFormat3(ExecutedTransfer)
  implicit val executedTransfersFormat: RootJsonFormat[ExecutedTransfers] = jsonFormat1(ExecutedTransfers.apply)
}
