/*
 * Copyright (c) 2018 Alexandru Nedelcu.
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

import monix.eval.Task
import org.alexn.vdp.models.{DownloadLinksJSON, HttpError, JSONError, WebError}
import org.http4s.{Header, MediaType, Method, Status, Uri}
import org.http4s.client.Client
import org.http4s.headers.Accept
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.util.CaseInsensitiveString

final class VimeoClient private (client: Client[Task]) extends Http4sClientDsl[Task] {
  /**
    * Fetches the download links from Vimeo for a public video with the
    * given `uid` and for which the download is allowed (Plus accounts and up).
    */
  def getDownloadLinks(uid: String, agent: Option[Header]): Task[Either[WebError, DownloadLinksJSON]] = {
    // For JSON deserialization
    import org.http4s.circe.CirceEntityDecoder._

    val base = Method.GET(
      Uri.unsafeFromString(s"https://vimeo.com/$uid?action=load_download_config"),
      Accept(MediaType.application.json))

    base.flatMap { req =>
      val request = req.putHeaders(
        agent.getOrElse(Header("User-Agent", "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:63.0) Gecko/20100101 Firefox/63.0")),
        Header("origin", "https://vimeo.com"),
        Header("Referer", s"https://vimeo.com/$uid"),
        Header("x-requested-with", "XMLHttpRequest")
      )

      client.fetch(request) {
        case Status.Successful(r) if r.status.code == 200 =>
          /*_*/r.attemptAs[DownloadLinksJSON]/*_*/.leftMap(e => JSONError(e.message)).value

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
}

object VimeoClient {
  /**
    * Builds a [[VimeoClient]] resource.
    */
  def apply(client: Client[Task]): Task[VimeoClient] =
    Task(new VimeoClient(client))
}
