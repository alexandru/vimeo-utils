package org.alexn.vdp.models

import io.circe.generic.extras.{ConfiguredJsonCodec, JsonKey}

/**
  * https://player.vimeo.com/video/300015010/config
  */
@ConfiguredJsonCodec
final case class VideoConfigJSON(
  video: VideoInfoJSON
)

@ConfiguredJsonCodec
final case class VideoInfoJSON(
  title: String,
  url: String,
  thumbs: VideoThumbsJSON
)

@ConfiguredJsonCodec
final case class VideoThumbsJSON(
  @JsonKey("base") base: Option[String],
  @JsonKey("1280") image1280: Option[String],
  @JsonKey("960") image960: Option[String],
  @JsonKey("640") image640: Option[String]
)