package io.github.asakaev.sebozune.frontend

import cats.effect.IO
import fs2.concurrent.Queue
import io.github.asakaev.sebozune.frontend.Main.State
import io.github.asakaev.sebozune.frontend.interop._
import io.github.asakaev.sebozune.shared.protocol.Payload.{ Login, Ping, Table, TablesSubscribe, TablesUnsubscribe }
import io.github.asakaev.sebozune.shared.protocol.{ Message, Payload }
import org.scalajs.dom.html.{ Button, Div }
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.{ Element, MouseEvent }
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all._

package object component {

  val nav: TypedTag[Element]     = tag("nav")
  val article: TypedTag[Element] = tag("article")
  val section: TypedTag[Element] = tag("section")
  val abbr: TypedTag[Element]    = tag("abbr")

  object Id {
    val Application = "application"
  }

  def appComponent(q: Queue[IO, Payload])(s: State): TypedTag[Div] =
    div(cls := "container")(
      h1(cls := "title")("sebozune"),
      p(cls := "subtitle")(s.id.toString()),
      counterComponent(s.in, s.out),
      tableComponent(s.tables),
      nav(
        cls := "level is-mobile",
        div(
          cls := "level-right",
          div(cls := "level-item", buttonComponent(q, "Auth 👌️")(_ => Login("admin", "admin")))
        ),
        div(
          cls := "level-right",
          div(cls := "level-item", buttonComponent(q, "Auth 🤷‍♂️")(_ => Login("1337", "h4x0r")))
        ),
        div(cls := "level-right", div(cls := "level-item", buttonComponent(q, "Ping 🍌")(e => Ping(e.clientX.toInt))))
      ),
      nav(
        cls := "level is-mobile",
        div(
          cls := "level-left",
          div(cls := "level-item", buttonComponent(q, "TablesSubscribe 🚀")(_ => TablesSubscribe))
        ),
        div(
          cls := "level-left",
          div(cls := "level-item", buttonComponent(q, "TablesUnsubscribe 💥")(_ => TablesUnsubscribe))
        )
      ),
      tilesComponent(s.log)
    )

  def counterComponent(in: Int, out: Int): TypedTag[Element] =
    nav(
      cls := "level is-mobile",
      div(
        cls := "level-item has-text-centered",
        div(
          p(cls := "heading", "In"),
          p(cls := "title", in)
        )
      ),
      div(
        cls := "level-item has-text-centered",
        div(
          p(cls := "heading", "Out"),
          p(cls := "title", out)
        )
      )
    )

  def buttonComponent(q: Queue[IO, Payload], text: String)(f: MouseEvent => Payload): TypedTag[Button] = {
    val cb = enqueue[MouseEvent, Payload](q)(f)
    button(cls := "button is-white", onclick := cb)(text)
  }

  def tableComponent(tables: List[Table]): TypedTag[HTMLElement] = tables match {
    case Nil => div()
    case xs =>
      table(
        cls := "table is-fullwidth",
        thead(
          tr(
            th("Id"),
            th("Name"),
            th("Participants")
          )
        ),
        tbody(
          xs.map { t =>
            tr(
              th(t.id),
              td(t.name),
              td(t.participants)
            )
          }
        )
      )
  }

  def tilesComponent(log: List[Message]): TypedTag[Div] =
    div(
      cls := "tile is-ancestor",
      log.map { m =>
        section(
          cls := "hero is-light",
          div(
            cls := "hero-body",
            div(
              cls := "container",
              h1(cls := "title", m.payload.toString()),
              h2(cls := "subtitle", m.sender.toString())
            )
          )
        )

      }
    )

}
