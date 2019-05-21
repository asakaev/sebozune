package io.github.asakaev.sebozune.frontend

import cats.effect.IO
import fs2._
import fs2.concurrent.Queue

package object interop {

  def enqueue[A, B](q: Queue[IO, B])(f: A => B): A => Unit =
    x => q.offer1(f(x)).unsafeRunAsyncAndForget()

  def enqueueIO[A, B](q: Queue[IO, B])(f: A => IO[Option[B]]): A => Unit =
    x => Stream.eval(f(x)).unNone.through(q.enqueue).compile.drain.unsafeRunAsyncAndForget()

}
