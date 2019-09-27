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

import io.circe.Decoder
import monix.eval.Task
import org.alexn.vdp.models.{DownloadLinksJSON, HttpError, JSONError, VimeoConfigJSON, VimeoConfig, WebError}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Accept
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Header, MediaType, Method, Status, Uri}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

final class VimeoClient private (client: Client[Task], config: VimeoConfig) extends Http4sClientDsl[Task] {
  private[this] val cache = Cached.unsafe[Either[WebError, AnyRef]]()

  /**
    * Fetches the download links from Vimeo for a public video with the
    * given `uid` and for which the download is allowed (Plus accounts and up).
    */
  def getDownloadLinks(uid: String, exp: FiniteDuration, agent: Option[Header], extra: Option[Header]*): Task[Either[WebError, DownloadLinksJSON]] =
    cache.getOrUpdate(uid + "/links/" + exp.toMillis.toString, exp, uncachedDownloadLinks(uid, agent, extra:_*))
      .asInstanceOf[Task[Either[WebError, DownloadLinksJSON]]]

  /**
    * Fetches thumbnail links.
    */
  def getPictures(uid: String, exp: FiniteDuration, agent: Option[Header], extra: Option[Header]*): Task[Either[WebError, VimeoConfigJSON]] =
    cache.getOrUpdate(uid + "/pictures/" + exp.toMillis.toString, exp, uncachedPictures(uid, agent, extra:_*))
      .asInstanceOf[Task[Either[WebError, VimeoConfigJSON]]]

  private def uncachedPictures(uid: String, agent: Option[Header], extra: Option[Header]*): Task[Either[WebError, VimeoConfigJSON]] =
    uncachedGET[VimeoConfigJSON](uid, s"https://api.vimeo.com/videos/$uid?access_token=${config.accessToken}&per_page=100", agent, extra:_*)

  private def uncachedDownloadLinks(uid: String, agent: Option[Header], extra: Option[Header]*): Task[Either[WebError, DownloadLinksJSON]] =
    uncachedGET[DownloadLinksJSON](uid, s"https://vimeo.com/$uid?action=load_download_config", agent, extra:_*)

  private def uncachedGET[T](uid: String, url: String, agent: Option[Header], extra: Option[Header]*)
    (implicit ev: Decoder[T]): Task[Either[WebError, T]] = {

    import org.http4s.circe.CirceEntityDecoder._

    val base = Method.GET(
      Uri.unsafeFromString(url),
      Accept(MediaType.application.json)
    )

    base.flatMap { req =>
      val request = req
        .putHeaders(
          Header("origin", "https://vimeo.com"),
          agent.getOrElse(Header("User-Agent", "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:63.0) Gecko/20100101 Firefox/63.0")),
          Header("Referer", s"https://vimeo.com/$uid"),
          Header("x-requested-with", "XMLHttpRequest")
        )
        .putHeaders(extra.collect { case Some(h) => h } :_*)

      logger.info("Making request: " + request.toString())

      client.fetch(request) {
        case Status.Successful(r) if r.status.code == 200 =>
          r.attemptAs[T].leftMap(e => JSONError(e.message)).value

        case r =>
          val contentType = r.headers.get(CaseInsensitiveString("Content-Type")).map(_.value)
          val process = r.bodyAsText
            .compile
            .fold(new StringBuilder)((acc, e) => acc.append(e))
            .map(_.toString())

          for (body <- process) yield
            Left(HttpError(r.status.code, body, contentType))
      }
    }
  }

  private[this] lazy val logger = LoggerFactory.getLogger(getClass)
}

object VimeoClient {
  /**
    * Builds a [[VimeoClient]] resource.
    */
  def apply(client: Client[Task], config: VimeoConfig): Task[VimeoClient] =
    Task(new VimeoClient(client, config))
}
