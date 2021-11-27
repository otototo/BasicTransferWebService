package server

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server._
import route.response.{Message, ResponseFormats}

class RouteComposition(routes: Route*)
  extends Directives
    with ResponseFormats {
  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler.default
      .mapRejectionResponse {
        case res@HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
          val message = ent.data.utf8String.replaceAll("\"", """\"""")
          res.withEntity(entity = HttpEntity(ContentTypes.`application/json`, s"""{"rejection": "$message"}"""))

        case x => x
      }

  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: IllegalArgumentException =>
        complete(HttpResponse(StatusCodes.BadRequest, entity = s"Provided input is not valid ${e.getMessage}"))
      case e =>
        extractUri { uri =>
          println(s"Request to $uri could not be handled normally. Received an exception ${e.getMessage}")
          complete(Message(StatusCodes.InternalServerError.intValue, "Unexpected error occurred."))
        }
    }

  val composition: Route = routes.reduceLeft(_ ~ _)
}
