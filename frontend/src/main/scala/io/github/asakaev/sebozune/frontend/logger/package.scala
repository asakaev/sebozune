package io.github.asakaev.sebozune.frontend

import cats.effect.IO
import io.github.asakaev.sebozune.shared.Console
import io.github.asakaev.sebozune.shared.protocol.Message

package object logger {
  val C: Console[IO] = Console[IO]
  import C.putStrLn

  val logIncoming: Message => IO[Unit] =
    (putStrLn _).compose[Message](m => s"< [$m]")

  val logOutgoing: Message => IO[Unit] =
    (putStrLn _).compose[Message](m => s"> [$m]")

  def logEffect[A]: A => IO[Unit] =
    (putStrLn _).compose[A](x => s"* [$x]")

}
