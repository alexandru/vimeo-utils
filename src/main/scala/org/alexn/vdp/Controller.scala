/*
 * Copyright (c) 2019 Alexandru Nedelcu.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alexn.vdp

import io.circe.Encoder
import io.circe.syntax._
import monix.eval.Task
import org.alexn.vdp.models.{DownloadLinksJSON, HttpError, JSONError, PicturesEntrySizeJSON, VimeoConfigJSON, WebError}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Header, HttpRoutes, Request, Response, Status}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

final class Controller private (vimeo: VimeoClient) extends Http4sDsl[Task] {
  val httpCacheExpiry = 1.day.toSeconds.toString

  lazy val routes = HttpRoutes.of[Task] {
    case GET -> Root =>
      Ok("Pong")

    case request @ GET -> Root / "redirect" / uid / name :? DownloadParam(download) =>
      findDownloads(request, uid, 1.minute) { info =>
        if (info.allowDownloads) {
          val search = name.toLowerCase.trim
          val file = info.sourceFile
            .find(_.publicName.toLowerCase.trim == search)
            .orElse(info.files.find(_.publicName.toLowerCase.trim == search))

          file match {
            case None =>
              notFound(s"file with name $name not found")

            case Some(value) =>
              val url = cleanURL(value.downloadURL, download.getOrElse(true))
              logger.info("Serving (video): " + url)
              Response[Task](Status.SeeOther)
                .putHeaders(Header("Location", url))
                .putHeaders(Header("Cache-Control", "no-cache"))
          }
        } else {
          notFound("allowDownloads=false")
        }
      }

    case request @ GET -> Root / "get" / uid =>
      findDownloads(request, uid, 24.hours)(jsonToResponse)

    case request @ GET -> Root / "config" / uid =>
      findThumbs(request, uid, 1.hour)(jsonToResponse)

    case request @ GET -> Root / "thumb" / uid :? MinWidth(minWidth) =>
      thumbView(request, "thumb", uid, minWidth)(_.link)

    case request @ GET -> Root / "thumb-play" / uid :? MinWidth(minWidth) =>
      thumbView(request, "thumb-play", uid, minWidth) { p =>
        p.linkWithPlayButton.getOrElse(p.link)
      }
  }

  private def thumbView(request: Request[Task], name: String, uid: String, minWidth: Option[Int])(
    f: PicturesEntrySizeJSON => String
  ) = {
    findThumbs(request, uid, 1.hour) { info =>
      info.pictures.sizes match {
        case Nil =>
          notFound(name + "/" + uid)

        case list =>
          val picture = minWidth match {
            case None =>
              list.maxBy(_.width)
            case Some(w) =>
              val sorted = list.sortBy(_.width)
              sorted.find(_.width >= w).getOrElse(sorted.last)
          }

          val url = f(picture)
          Response[Task](Status.SeeOther)
            .putHeaders(Header("Location", url))
            .putHeaders(Header("Cache-Control", "max-age=" + httpCacheExpiry))
      }
    }
  }

  private def jsonToResponse[A : Encoder](response: A): Response[Task] =
    Response[Task](Status.Ok)
      .withEntity(response.asJson)
      .putHeaders(Header("Cache-Control", "max-age=" + httpCacheExpiry))

  private def findThumbs(
    request: Request[Task],
    uid: String,
    exp: FiniteDuration
  )(f: VimeoConfigJSON => Response[Task]): Task[Response[Task]] = {

    find(request, f) { (agent, forwardedFor) =>
      vimeo.getPictures(uid, exp, agent, forwardedFor)
    }
  }

  private def findDownloads(
    request: Request[Task],
    uid: String,
    exp: FiniteDuration
  )(f: DownloadLinksJSON => Response[Task]) = {

    find(request, f) { (agent, forwardedFor) =>
      vimeo.getDownloadLinks(uid, exp, agent, forwardedFor)
    }
  }

  type UserAgent = Header
  type ForwardedFor = Header

  private def find[T](request: Request[Task], f: T => Response[Task])(
    generate: (Option[UserAgent],
               Option[ForwardedFor]) => Task[Either[WebError, T]]
  ) = {

    System.realIP.flatMap { serverIP =>
      val agent = request.headers.get(CaseInsensitiveString("User-Agent"))
      val currentForwardedFor =
        request.headers
          .get(CaseInsensitiveString("X-Forwarded-For"))
          .orElse(request.headers.get(CaseInsensitiveString("X-Client-IP")))
          .orElse(request.headers.get(CaseInsensitiveString("X-ProxyUser-Ip")))

      val forwardedFor = serverIP.filter(_.nonEmpty) match {
        case None => currentForwardedFor
        case Some(proxyIP) =>
          currentForwardedFor match {
            case None =>
              request.remoteAddr.map(
                ip => Header("X-Forwarded-For", ip + ", " + proxyIP)
              )
            case Some(header) =>
              Some(Header("X-Forwarded-For", header.value + ", " + proxyIP))
          }
      }

      generate(agent, forwardedFor).map {
        case Left(HttpError(status, body, contentType)) =>
          // Core web-service triggered an HTTP error, mirroring it as is
          val r =
            Response[Task](Status.fromInt(status).right.get).withEntity(body)
          contentType.fold(r)(ct => r.putHeaders(Header("Content-Type", ct)))

        case Left(JSONError(msg)) =>
          // Exceptions was thrown somewhere, not good
          logger.warn("Core web service problem — {}", msg)
          Response[Task](Status.BadGateway)
            .withEntity("502 Bad Gateway (Vimeo)")

        case Left(error) =>
          // Exceptions was thrown somewhere, not good
          logger.warn("Unexpected error: {}", error.toString)
          Response[Task](Status.InternalServerError)
            .withEntity("500 Internal Server Error")

        case Right(info) =>
          f(info)
      }
    }
  }

  private def cleanURL(url: String, download: Boolean): String =
    if (download) {
      if (!url.contains("download"))
        url + "&download=1"
      else
        url
    } else {
      url.replaceAll("[&]download[=]\\w+", "")
    }

  private object DownloadParam
    extends OptionalQueryParamDecoderMatcher[Boolean]("download")

  private object MinWidth
    extends OptionalQueryParamDecoderMatcher[Int]("width")

  private[this] def notFound(reason: String) = {
    logger.warn(s"Not Found: $reason")
    Response[Task](Status.NotFound).withEntity("404 Not Found")
  }

  private[this] val logger =
    LoggerFactory.getLogger(getClass)
}

object Controller {

  /** Builds a [[Controller]]. */
  def apply(vimeo: VimeoClient): Task[Controller] =
    Task(new Controller(vimeo))
}
