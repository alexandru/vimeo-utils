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

import java.io.{BufferedReader, InputStreamReader}
import monix.eval.Task
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

object System {
  private[this] val cache = Cached.unsafe[String]()

  /** Gets the real IP. */
  val realIP: Task[Option[String]] = {
    cache.getOrUpdate("ip", 1.hour, Task.evalAsync(unsafeGetIP()))
      .redeem(
        e => {
          logger.error("Unexpected error while fetching IP", e)
          None
        },
        Some(_)
      )
  }

  private def unsafeGetIP(): String = {
    unsafeExecute("dig +short myip.opendns.com @resolver1.opendns.com")
  }

  private def unsafeExecute(cmd: String) = {
    val p = Runtime.getRuntime.exec(cmd)
    if (p.waitFor() != 0)
      throw new RuntimeException("Process exited in error!")

    val in = new BufferedReader(new InputStreamReader(p.getInputStream))
    val sb = new StringBuilder()

    var line = ""
    while (line != null) {
      line = in.readLine()
      if (line != null) sb.append(line)
    }
    sb.toString()
  }

  private[this] lazy val logger =
    LoggerFactory.getLogger(getClass)
}
