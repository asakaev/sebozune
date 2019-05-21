package io.github.asakaev.sebozune.shared

import cats.effect.IO

trait Console[F[_]] {
  def putStrLn(s: String): F[Unit]
}

object Console {
  def apply[F[_]](implicit ev: Console[F]): Console[F] = ev

  implicit val consoleIO: Console[IO] = s => IO(println(s))
}
