package com.github.kxbmap.configs

import com.typesafe.config.ConfigFactory
import org.scalacheck.Arbitrary
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.PropertyChecks


class BytesSpec extends FlatSpec with ShouldMatchers with PropertyChecks {

  behavior of "Bytes"

  it should "get from Config" in {
    val config = ConfigFactory.load("test-data")
    config.get[Bytes]("bytes.value") should be (Bytes(1024L))
    config.get[List[Bytes]]("bytes.values") should be (List(Bytes(100L), Bytes(1000000L)))
  }

  implicit val BytesArb = Arbitrary {
    Arbitrary.arbitrary[Long].map(Bytes.apply)
  }

  it should "be Ordering" in {
    forAll { (bs: List[Bytes]) =>
      bs.sorted.map(_.value) should be (bs.map(_.value).sorted)
    }
  }

  it should "be Ordered" in {
    forAll { (l: Bytes, r: Bytes) =>
      (l compare r) should be (l.value compare r.value)
    }
  }

}
