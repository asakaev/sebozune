package io.github.asakaev.sebozune.shared.protocol

import java.util.concurrent.TimeUnit

import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }

import scala.concurrent.duration.FiniteDuration

sealed trait Payload

object Payload {
  case class Ping(seq: Int)                            extends Payload
  case class Pong(seq: Int)                            extends Payload
  case class Login(username: String, password: String) extends Payload
  case class LoggedIn(role: String)                    extends Payload
  case class TableList(tables: List[Table])            extends Payload
  case object TablesSubscribe                          extends Payload
  case object TablesUnsubscribe                        extends Payload
  case object Unauthorized                             extends Payload
  case object TopicInit                                extends Payload

  case class Table(id: Int, name: String, participants: Int)

  implicit val decodePayload: Decoder[Payload] = deriveDecoder
  implicit val encodePayload: Encoder[Payload] = deriveEncoder

  implicit val decodeTable: Decoder[Table] = deriveDecoder
  implicit val encodeTable: Encoder[Table] = deriveEncoder

  implicit val decodeFiniteDuration: Decoder[FiniteDuration] =
    Decoder.decodeLong.emap { l =>
      Right(FiniteDuration(l, TimeUnit.MILLISECONDS))
    }
  implicit val encodeFiniteDuration: Encoder[FiniteDuration] =
    Encoder.encodeLong.contramap[FiniteDuration](_.toMillis)

}
