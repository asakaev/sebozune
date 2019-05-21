package io.github.asakaev.sebozune.frontend.transport

import cats.effect.{ Concurrent, IO }
import fs2._
import fs2.concurrent.Queue
import io.github.asakaev.sebozune.frontend.interop.enqueue
import org.scalajs.dom
import org.scalajs.dom.MessageEvent
import org.scalajs.dom.raw.Event

object WebSocket {

  def of(url: String)(implicit F: Concurrent[IO]): IO[(Stream[IO, String], Pipe[IO, String, INothing])] =
    for {
      q      <- Queue.unbounded[IO, String]
      socket <- IO(new dom.WebSocket(url))

      _ <- IO.async[Event] { cb =>
        socket.onopen = { e: Event =>
          cb(Right(e))
        }
      }
      _ <- IO {
        socket.onmessage = enqueue[MessageEvent, String](q)(_.data.toString)
      }

      // TODO: use socket.bufferedAmount to implement back-pressure
      pipe = (_: Stream[IO, String]).evalTap(s => IO(socket.send(s))).drain

    } yield q.dequeue -> pipe

}
