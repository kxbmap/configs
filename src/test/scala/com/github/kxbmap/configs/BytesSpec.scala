package com.github.kxbmap.configs

import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers


class BytesSpec extends FlatSpec with ShouldMatchers {

  behavior of "Bytes"

  it should "get from Config" in {
    val config = ConfigFactory.load("test-data")
    config.get[Bytes]("bytes.value") should be (Bytes(1024L))
    config.get[List[Bytes]]("bytes.values") should be (List(Bytes(100L), Bytes(1000000L)))
  }

}
