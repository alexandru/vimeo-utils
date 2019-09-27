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

import cats.arrow.FunctionK
import cats.effect.ConcurrentEffect
import fs2.Stream
import monix.eval.Task
import org.alexn.vdp.models.AppConfig
import org.http4s.implicits._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{AutoSlash, CORS, Logger}
import monix.execution.Scheduler.Implicits.global

object Server extends Http4sDsl[Task] {

  def stream(implicit F: ConcurrentEffect[Task]): Stream[Task, Nothing] = {
    for {
      config <- Stream.eval(AppConfig.loadFromEnv)
      client <- BlazeClientBuilder[Task](global).stream
      vimeo <- Stream.eval(VimeoClient(client, config.vimeo))
      controller <- Stream.eval(Controller(vimeo))

      // With middleware in place
      httpApp = Logger(logHeaders = true, logBody = false, FunctionK.id[Task])(
        CORS(AutoSlash(controller.routes)).orNotFound
      )

      exitCode <- BlazeServerBuilder[Task]
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(httpApp)
        .serve
    } yield {
      exitCode
    }
  }.drain
}
