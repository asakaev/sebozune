package io.github.asakaev.sebozune.frontend.transport

import java.util.Base64

import cats.effect.IO
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax

object Http {

  case class BasicCredentials(username: String, password: String)

  val bytes: String => Array[Byte] =
    _.map(_.toByte).toArray

  val base64: Array[Byte] => String =
    Base64.getEncoder.encodeToString

  val encode: String => String =
    base64.compose(bytes)

  def request(url: String, obc: Option[BasicCredentials]): IO[XMLHttpRequest] =
    obc match {
      case None => IO.fromFuture(IO(Ajax.get(url)))
      case Some(bc) =>
        val basic = s"Basic ${encode(s"${bc.username}:${bc.password}")}"
        IO.fromFuture(IO(Ajax.get(url = url, headers = Map("Authorization" -> basic))))
    }

}
