package io.github.asakaev.sebozune.frontend

import java.util.UUID

import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._
import fs2.concurrent.Queue
import fs2.{ Pipe, Stream }
import io.circe.parser._
import io.circe.syntax._
import io.github.asakaev.sebozune.frontend.ClientPipe._
import io.github.asakaev.sebozune.frontend.client.hostname
import io.github.asakaev.sebozune.frontend.component.{ appComponent, Id }
import io.github.asakaev.sebozune.frontend.logger._
import io.github.asakaev.sebozune.frontend.transport.Http.BasicCredentials
import io.github.asakaev.sebozune.frontend.transport.{ Http, WebSocket }
import io.github.asakaev.sebozune.shared.Model.identity
import io.github.asakaev.sebozune.shared.protocol.Payload.Table
import io.github.asakaev.sebozune.shared.protocol.{ Message, Payload }
import io.github.asakaev.sebozune.shared.{ Console, ServerPath }
import org.scalajs.dom.html.Div
import scalatags.JsDom

object Main extends IOApp {

  type StateRender = State => JsDom.TypedTag[Div]

  case class State(id: UUID, in: Int, out: Int, log: List[Message], tables: List[Table], subscribed: Boolean)

  val messagesLimit = 4
  val bc            = BasicCredentials("admin", "admin")

  def renderPipe(sr: StateRender): Pipe[IO, (State, Option[Message]), Message] =
    _.evalMap { case (s, m) => OverLook.render(Id.Application, sr(s)).map(_ => m) }.unNone

  def url(hostname: String): String =
    s"ws://$hostname:9000/${ServerPath.WsApi}"

  override def run(args: List[String]): IO[ExitCode] =
    for {
      id        <- identity[IO]
      _         <- Http.request("/auth", Some(bc))
      hostname  <- hostname
      webSocket <- WebSocket.of(url(hostname))
      _         <- Console[IO].putStrLn(id.toString)
      q         <- Queue.unbounded[IO, Payload]

      (source, sink) = webSocket

      ws = source
        .mapFilter(decode[Message](_).toOption)
        .drop(1)
        .filter { case Message(sender, _) => sender != id }
        .evalTap(logIncoming)
        .map(Left(_))

      ui = q.dequeue.map(p => Message(id, p)).map(Right(_))

      zero     = State(id, 0, 0, List.empty, List.empty, subscribed = false)
      scanned  = Stream(zero -> None) ++ ws.merge(ui).through(stateScan(zero, messagesLimit))
      rendered = scanned.through(renderPipe(appComponent(q)))

      app = rendered.evalTap(logOutgoing).map(_.asJson.noSpaces).through(sink)

      _ <- app.compile.drain
    } yield ExitCode.Success

}
