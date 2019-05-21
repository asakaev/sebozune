package io.github.asakaev.sebozune.backend

import java.util.UUID

import cats.Applicative
import cats.effect.concurrent.Ref
import io.github.asakaev.sebozune.shared.protocol.Message
import io.github.asakaev.sebozune.shared.protocol.Payload._
import fs2._

object ServerPipe {

  val init = State(Set.empty, List(Table(1, "Alice in Wonderland", 0), Table(2, "Through the Looking-Glass", 42)))

  case class State(subscribers: Set[UUID], tables: List[Table])

  def valid[F[_]: Applicative](username: String, password: String): F[Boolean] =
    Applicative[F].pure(username == "admin" && password == "admin")

  def pipe[F[_]: Applicative](id: UUID, ref: Ref[F, State]): Pipe[F, Message, Message] =
    _.flatMap {
      case Message(_, Ping(seq)) => Stream(Message(id, Pong(seq)))
      case Message(_, Login(u, p)) =>
        Stream.eval(valid[F](u, p)).map {
          case true  => Message(id, LoggedIn("admin"))
          case false => Message(id, Unauthorized)
        }
      case Message(sender, TablesSubscribe) =>
        Stream.eval {
          ref.update(s => s.copy(subscribers = s.subscribers + sender))
        } >> Stream.eval(ref.get).map(s => Message(id, TableList(s.tables)))
      case Message(sender, TablesUnsubscribe) =>
        Stream.eval {
          ref.update(s => s.copy(subscribers = s.subscribers - sender))
        } >> Stream.eval(ref.get).map(s => Message(id, TableList(s.tables)))
      case m => Stream(m)
    }

}
