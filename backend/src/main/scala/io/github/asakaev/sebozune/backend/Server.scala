package io.github.asakaev.sebozune.backend

import java.util.concurrent.Executors

import cats.effect.concurrent.Ref
import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import cats.implicits._
import fs2.Stream
import fs2.concurrent.Topic
import io.github.asakaev.sebozune.backend.Authentication.{ authMiddleware, cookieMiddleware }
import io.github.asakaev.sebozune.shared.Model.identity
import io.github.asakaev.sebozune.shared.protocol.Message
import io.github.asakaev.sebozune.shared.protocol.Payload.TopicInit
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext

object Server {

  val blockingEc: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    for {
      id    <- Stream.eval(identity[F])
      topic <- Stream.eval(Topic[F, Message](Message(id, TopicInit)))
      ref   <- Stream.eval(Ref.of(ServerPipe.init))
      authAlg = Auth.impl[F]

      httpApp = (
        Routes.staticRoutes[F](blockingEc) <+>
          cookieMiddleware[F].apply(Routes.wsRoutes[F](id, topic, ref)) <+>
          authMiddleware[F].apply(Routes.authRoutes[F](authAlg))
      ).orNotFound

      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = false)(httpApp)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(9000, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
