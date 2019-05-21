package io.github.asakaev.sebozune.frontend

import cats.effect.IO
import fs2._
import io.github.asakaev.sebozune.frontend.Main.State
import io.github.asakaev.sebozune.shared.protocol.Message
import io.github.asakaev.sebozune.shared.protocol.Payload.{ TableList, TablesSubscribe, TablesUnsubscribe }

object ClientPipe {

  def appendLog[A](n: Int)(x: A, xs: List[A]): List[A] =
    (x :: xs).take(n)

  def stateScan(init: State, n: Int): Pipe[IO, Either[Message, Message], (State, Option[Message])] =
    _.mapAccumulate(init) {
      case (s, Left(Message(_, TableList(ts)))) if s.subscribed =>
        s.copy(tables = ts) -> None
      case (s, Left(m)) =>
        s.copy(in = s.in + 1, log = appendLog(n)(m, s.log)) -> None
      case (s, Right(m @ Message(_, TablesSubscribe))) =>
        s.copy(subscribed = true) -> Some(m)
      case (s, Right(m @ Message(_, TablesUnsubscribe))) =>
        s.copy(subscribed = false, tables = Nil) -> Some(m)
      case (s, Right(m)) =>
        s.copy(out = s.out + 1, log = appendLog(n)(m, s.log)) -> Some(m)
    }

}
