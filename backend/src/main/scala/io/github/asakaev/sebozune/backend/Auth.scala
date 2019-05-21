package io.github.asakaev.sebozune.backend

import cats.Applicative
import cats.implicits._
import org.http4s.BasicCredentials

trait Auth[F[_]] {
  def token(bc: BasicCredentials): F[Option[String]]
}

object Auth {
  val users: Map[String, String] = Map("admin" -> "admin")
  val jwt: String                = "JWT"

  implicit def apply[F[_]](implicit ev: Auth[F]): Auth[F] = ev

  def impl[F[_]: Applicative]: Auth[F] =
    bc => users.get(bc.username).filter(_ == bc.password).map(_ => jwt).pure[F]
}
