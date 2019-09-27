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

package org.alexn.vdp.models

import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.alexn.vdp.IPUtils

/**
  * Represents either an IPv4 or an IPv6 address.
  */
final case class IP(address: String) {
  /**
    * Returns `true` if this is a valid public IP, `false` otherwise.
    */
  def isPublicIP: Boolean =
    IPUtils.validatePublicIP(address)
}

object IP {
  /**
    * Explicit JSON encoder for this ADT, see
    * [[https://circe.github.io/circe/codecs/adt.html the docs]]
    */
  implicit val encodeJSON: Encoder[IP] =
    Encoder.instance(_.address.asJson)

  /**
    * Explicit JSON decoder for this ADT, see
    * [[https://circe.github.io/circe/codecs/adt.html the docs]]
    */
  implicit val decodeJSON: Decoder[IP] =
    Decoder[String].map(IP(_))
}
