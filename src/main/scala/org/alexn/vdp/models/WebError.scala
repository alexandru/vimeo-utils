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

package org.alexn.vdp.models

import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.charset.StandardCharsets

import io.circe.generic.JsonCodec

/**
  * Models errors that can be thrown by web services:
  *
  *   - HTTP code errors ([[HttpError]])
  *   - JSON parsing errors ([[JSONError]])
  *   - ...
  */
@JsonCodec
sealed trait WebError

@JsonCodec
final case class HttpError(status: Int, body: String, contentType: Option[String])
  extends WebError

@JsonCodec
final case class JSONError(message: String)
  extends WebError

@JsonCodec
final case class UncaughtException(cls: String, message: String, trace: String)
  extends WebError

object UncaughtException {
  /**
    * Builds a value from a plain exception.
    */
  def apply(e: Throwable): UncaughtException = {
    val bs = new ByteArrayOutputStream
    val ps = new PrintStream(bs, true, "UTF-8")
    e.printStackTrace(ps)
    val trace = new String(bs.toByteArray, StandardCharsets.UTF_8)
    UncaughtException(e.getClass.getName, e.getMessage, trace)
  }
}
