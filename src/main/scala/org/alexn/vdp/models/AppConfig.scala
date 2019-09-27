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

import java.io.File
import com.typesafe.config.{Config, ConfigFactory}
import monix.eval.Task

/**
  * Global application config.
  */
final case class AppConfig(configSource: ConfigSource,
                           http: HttpConfig,
                           vimeo: VimeoConfig)

/**
  * Address for the server to listen for incoming connections.
  */
final case class HttpConfig(host: String, port: Int)

/**
  * Configuration for Vimeo's API.
  */
final case class VimeoConfig(accessToken: String)

/**
  * Indicates the source from where the Typesafe Config must be loaded.
  */
sealed trait ConfigSource

object ConfigSource {
  case class Resource(name: String) extends ConfigSource
  case class Path(name: String) extends ConfigSource
  case object Unknown extends ConfigSource
}

object AppConfig {
  def loadFromEnv: Task[AppConfig] =
    Task.suspend {
      val (source, config) = loadRawFromEnv()
      load(config).map(_.copy(configSource = source))
    }

  def load(config: Config): Task[AppConfig] = Task {
    AppConfig(
      configSource = ConfigSource.Unknown,
      http = HttpConfig(
        host = config.getString("http.host"),
        port = config.getInt("http.port")
      ),
      vimeo = VimeoConfig(
        accessToken = config.getString("vimeo.accessToken")
      )
    )
  }

  private def getConfigSource: ConfigSource =
    Option(System.getProperty("config.file")) match {
      case Some(path) if new File(path).exists() =>
        ConfigSource.Path(path)

      case _ =>
        val opt1 = Option(System.getProperty("ENV", "")).filter(_.nonEmpty)
        val opt2 = Option(System.getProperty("env", "")).filter(_.nonEmpty)

        opt1.orElse(opt2) match {
          case Some(envName) =>
            val name = s"application.${envName.toLowerCase}.conf"
            ConfigSource.Resource(name)
          case None =>
            ConfigSource.Resource("application.local.conf")
        }
    }

  private def loadRawFromEnv(): (ConfigSource, Config) = {
    val default = ConfigFactory.load("application.default.conf").resolve()
    val (source, config) = getConfigSource match {
      case ref @ ConfigSource.Path(path) =>
        (ref, ConfigFactory.parseFile(new File(path)).resolve())

      case ref @ ConfigSource.Resource(name) =>
        (ref, ConfigFactory.load(name).resolve())

      case ConfigSource.Unknown =>
        (ConfigSource.Unknown, ConfigFactory.load().resolve())
    }
    (source, config.withFallback(default))
  }
}
