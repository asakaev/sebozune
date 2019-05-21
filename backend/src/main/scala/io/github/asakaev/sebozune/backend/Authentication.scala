package io.github.asakaev.sebozune.backend

import cats.data.{ Kleisli, OptionT }
import cats.effect.Sync
import cats.{ Applicative, Monad }
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.{ headers, BasicCredentials, Request }

object Authentication {

  case class Token(jwt: String)

  def valid(s: String): Boolean = s == "JWT"

  def cookieAuth[F[_]: Applicative]: Kleisli[({ type T[A] = OptionT[F, A] })#T, Request[F], Token] =
    Kleisli((req: Request[F]) => {
      val cs: Option[String] = for {
        header <- headers.Cookie.from(req.headers)
        cookie <- header.values.toList.find(_.name == "token")
      } yield cookie.content

      OptionT.fromOption(cs.filter(valid).map(Token))
    })

  def cookieMiddleware[F[_]: Monad]: AuthMiddleware[F, Token] =
    AuthMiddleware.withFallThrough(cookieAuth[F])

  def authMiddleware[F[_]: Sync]: AuthMiddleware[F, BasicCredentials] =
    BasicAuth[F, BasicCredentials]("realm", bc => Applicative[F].pure(Some(bc)))
}
