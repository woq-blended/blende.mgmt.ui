package blended.mgmt.ui.server.internal

import akka.http.scaladsl.model
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Source, StreamConverters}

import scala.util.{Failure, Success, Try}

class UiRoute(cl: ClassLoader) {

  // All resources will be served from the classpath
  val route : Route = extractUnmatchedPath { path =>
    val resourcePath = if (path.toString().endsWith("/"))  {
      s"webapp${path.toString()}index.html"
    } else {
      s"webapp${path.toString()}"
    }

    val contentType = if (resourcePath.endsWith("html")) ContentTypes.`text/html(UTF-8)` else ContentTypes.`application/octet-stream`

    Try(cl.getResourceAsStream(resourcePath)) match {
      case Success(found) =>
        Option(found) match {
          case Some(s) =>
            complete(model.HttpResponse(
              entity = HttpEntity(
                contentType = contentType,
                data = StreamConverters.fromInputStream( () => s )
              )
            ))
          case None =>
            complete(StatusCodes.NotFound)
        }
      case Failure(e) =>
        complete(StatusCodes.NotFound)
    }
  }
}
