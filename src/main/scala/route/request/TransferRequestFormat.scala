package route.request

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TransferRequestFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val transferRequestFormat: RootJsonFormat[TransferRequest] = jsonFormat3(TransferRequest)
}
