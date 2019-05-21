package io.github.asakaev.sebozune.shared

import java.util.UUID

import cats.effect.Sync

case object Model {
  def identity[F[_]: Sync]: F[UUID] = Sync[F].delay(UUID.randomUUID())
}
