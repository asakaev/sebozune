package io.github.asakaev.sebozune.shared.protocol

import java.util.UUID

import cats.syntax.either._
import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }

case class Message(sender: UUID, payload: Payload)

object Message {

  implicit val decodeMessage: Decoder[Message] = deriveDecoder
  implicit val encodeMessage: Encoder[Message] = deriveEncoder

  implicit val decodeUUID: Decoder[UUID] =
    Decoder.decodeString.emap { s =>
      Either.catchNonFatal(UUID.fromString(s)).leftMap(_ => "UUID")
    }

  implicit val encodeUUID: Encoder[UUID] =
    Encoder.encodeString.contramap[UUID](_.toString)
}
