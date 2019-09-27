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

import java.net.InetAddress
import org.alexn.vdp.models.IP
import org.http4s.Request
import org.http4s.util.CaseInsensitiveString
import scala.util.control.NonFatal

object IPUtils {
  /**
    * Extracts the public IP from an HTTP request, by parsing
    * the `X-Forwarded-For` header.
    */
  def getRealIP[F[_]](request: Request[F]): Option[IP] =
    request.headers.get(CaseInsensitiveString("X-Forwarded-For")) match {
      case Some(header) =>
        header.value.split("\\s*,\\s*").find(validatePublicIP) match {
          case ip @ Some(_) =>
            ip.map(IP(_))
          case None =>
            request.remoteAddr.map(IP(_))
        }
      case None =>
        request.remoteAddr.map(IP(_))
    }

  /**
    * Returns `true` if the given IP address is a real and public IP
    * address — e.g. `127.0.0.1` isn't allowed.
    */
  def validatePublicIP(ip: String): Boolean = {
    try {
      val parsed = InetAddress.getByName(ip)
      !(parsed.isLoopbackAddress || parsed.isSiteLocalAddress)
    } catch {
      case NonFatal(_) => false
    }
  }
}
