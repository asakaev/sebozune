package io.github.asakaev.sebozune.shared.protocol

import io.circe.Json
import io.circe.syntax._
import io.github.asakaev.sebozune.shared.protocol.Payload.Ping
import org.scalatest.FunSuite

class PayloadSuite extends FunSuite {
  test("payload encoder") {
    val p: Payload = Ping(1)
    val expected   = Json.obj("Ping" -> Json.obj("seq" -> Json.fromInt(1)))
    assert(p.asJson == expected)
  }

}
