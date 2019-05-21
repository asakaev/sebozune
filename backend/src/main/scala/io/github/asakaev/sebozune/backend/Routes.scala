package io.github.asakaev.sebozune.backend

import java.io.File
import java.util.UUID

import cats.Applicative
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.concurrent.Topic
import fs2.{ Pipe, Stream }
import io.github.asakaev.sebozune.backend.Authentication.Token
import io.github.asakaev.sebozune.backend.ServerPipe.State
import io.github.asakaev.sebozune.shared.{ protocol, ServerPath }
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

import scala.concurrent.ExecutionContext

object Routes {

  def staticRoutes[F[_]: Sync: ContextShift](blockingEc: ExecutionContext): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case request @ GET -> Root =>
        StaticFile
          .fromFile(new File("../../frontend/src/main/assets/index.html"), blockingEc, Some(request))
          .getOrElseF(NotFound())
      case request @ GET -> Root / "sebozune-frontend-fastopt.js" =>
        StaticFile
          .fromFile(
            new File("../../frontend/.js/target/scala-2.12/sebozune-frontend-fastopt.js"),
            blockingEc,
            Some(request)
          )
          .getOrElseF(NotFound())
      case request @ GET -> Root / "sebozune-frontend-fastopt.js.map" =>
        StaticFile
          .fromFile(
            new File("../../frontend/.js/target/scala-2.12/sebozune-frontend-fastopt.js.map"),
            blockingEc,
            Some(request)
          )
          .getOrElseF(NotFound())
      case GET -> Root / "favicon.ico" =>
        Ok()
    }
  }

  def authRoutes[F[_]: Sync: Applicative](A: Auth[F]): AuthedService[BasicCredentials, F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    AuthedService[BasicCredentials, F] {
      case GET -> Root / "auth" as bc =>
        for {
          token <- A.token(bc)
          rep <- token match {
            case None      => Forbidden()
            case Some(jwt) => Ok().map(_.addCookie(ResponseCookie(name = "token", content = jwt, httpOnly = true)))
          }
        } yield rep
    }
  }

  def wsRoutes[F[_]: Applicative](
    id: UUID,
    topic: Topic[F, protocol.Message],
    ref: Ref[F, State]
  ): AuthedService[Token, F] = {
    import io.circe.parser._
    import io.circe.syntax._

    val dsl = new Http4sDsl[F] {}
    import dsl._

    AuthedService[Token, F] {
      case GET -> Root / ServerPath.WsApi as _ =>
        val toClient: Stream[F, WebSocketFrame] =
          topic.subscribe(1).map(_.asJson.noSpaces).map(Text(_))

        val fromClient: Pipe[F, WebSocketFrame, Unit] =
          _.mapFilter {
            case Text(s, _) => decode[protocol.Message](s).toOption
            case _          => None
          }.through(ServerPipe.pipe[F](id, ref)).through(topic.publish)

        WebSocketBuilder[F].build(toClient, fromClient)
    }

  }

}
