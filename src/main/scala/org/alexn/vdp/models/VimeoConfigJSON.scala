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

import io.circe.generic.extras.{ConfiguredJsonCodec, JsonKey}

@ConfiguredJsonCodec
final case class VimeoConfigJSON(
  @JsonKey("uri")
  uri: String,
  @JsonKey("name")
  name: String,
  @JsonKey("description")
  description: Option[String],
  @JsonKey("link")
  link: String,
  @JsonKey("duration")
  duration: Option[Long],
  @JsonKey("width")
  width: Option[Int],
  @JsonKey("height")
  height: Option[Int],
  @JsonKey("pictures")
  pictures: VimeoPicturesJSON
)

@ConfiguredJsonCodec
final case class VimeoPicturesJSON(
  @JsonKey("uri")
  uri: String,
  @JsonKey("active")
  active: Boolean,
  @JsonKey("type")
  entryType: Option[String],
  @JsonKey("sizes")
  sizes: List[PicturesEntrySizeJSON],
  @JsonKey("resource_key")
  resourceKey: Option[String]
)

@ConfiguredJsonCodec
final case class PicturesEntrySizeJSON(
  @JsonKey("width")
  width: Int,
  @JsonKey("height")
  height: Int,
  @JsonKey("link")
  link: String,
  @JsonKey("link_with_play_button")
  linkWithPlayButton: Option[String]
)