package io.github.asakaev.sebozune.backend

import java.util.UUID

import cats.effect.IO
import cats.effect.concurrent.Ref
import io.github.asakaev.sebozune.backend.ServerPipe._
import io.github.asakaev.sebozune.shared.protocol.Message
import io.github.asakaev.sebozune.shared.protocol.Payload.{ Ping, Pong }
import fs2._
import org.scalatest.AsyncFunSuite

class ServerPipeSuite extends AsyncFunSuite {
  test("pipe ping-pong") {
    val uuid = UUID.fromString("0db0cfd8-97c2-40cb-8857-81a71c143a50")

    Stream
      .eval(Ref.of[IO, State](init))
      .flatMap { r =>
        Stream(Message(uuid, Ping(42))).through(pipe(uuid, r))
      }
      .compile
      .toList
      .map(m => assert(m == Message(uuid, Pong(42)) :: Nil))
      .unsafeToFuture()

  }

}
