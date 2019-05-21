package io.github.asakaev.sebozune.frontend

import cats.effect.IO
import org.scalajs.dom

package object client {
  val hostname: IO[String] = IO(dom.window.location.hostname)
}
