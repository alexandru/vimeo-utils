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

import io.circe.generic.extras.{ConfiguredJsonCodec, JsonKey}

@ConfiguredJsonCodec
final case class RawFileJSON(
  @JsonKey("width") width: Option[Int],
  @JsonKey("height") height: Option[Int],
  @JsonKey("size") size: Option[String],
  @JsonKey("public_name") publicName: String,
  @JsonKey("extension") extension: String,
  @JsonKey("download_name") downloadName: String,
  @JsonKey("download_url") downloadURL: String,
  @JsonKey("is_cold") isCold: Option[Boolean],
  @JsonKey("is_defrosting") isDefrosting: Option[Boolean],
  @JsonKey("range") range: Option[String]
)

@ConfiguredJsonCodec
final case class DownloadLinksJSON(
  @JsonKey("allow_downloads") allowDownloads: Boolean,
  @JsonKey("files") files: List[RawFileJSON],
  @JsonKey("source_file") sourceFile: Option[RawFileJSON]
)
