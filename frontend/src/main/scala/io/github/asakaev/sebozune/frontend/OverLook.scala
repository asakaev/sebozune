package io.github.asakaev.sebozune.frontend

import cats.effect.IO
import org.scalajs.dom
import org.scalajs.dom.html.Div
import org.scalajs.dom.raw.Element
import scalatags.JsDom.TypedTag

object OverLook {

  def element[A](id: String): IO[Option[A]] =
    IO(Option(dom.document.getElementById(id).asInstanceOf[A]))

  def update(e: Element, div: TypedTag[Div]): IO[Unit] =
    if (e.childElementCount == 0) IO(e.appendChild(div.render))
    else IO(e.replaceChild(div.render, e.firstChild))

  def render(id: String, div: TypedTag[Div]): IO[Unit] =
    for {
      maybeElement <- element[Element](id)
      _            <- maybeElement.map(update(_, div)).getOrElse(IO.unit)
    } yield ()

}
