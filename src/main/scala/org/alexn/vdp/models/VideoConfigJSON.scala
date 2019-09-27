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

/**
  * https://player.vimeo.com/video/300015010/config
  */
@ConfiguredJsonCodec
final case class VideoConfigJSON(
  @JsonKey("video") video: VideoInfoJSON
)

@ConfiguredJsonCodec
final case class VideoInfoJSON(
  @JsonKey("title") title: Option[String],
  @JsonKey("url") url: Option[String],
  @JsonKey("thumbs") thumbs: VideoThumbsJSON
)

@ConfiguredJsonCodec
final case class VideoThumbsJSON(
  @JsonKey("base") base: Option[String],
  @JsonKey("1280") image1280: Option[String],
  @JsonKey("960") image960: Option[String],
  @JsonKey("640") image640: Option[String]
)