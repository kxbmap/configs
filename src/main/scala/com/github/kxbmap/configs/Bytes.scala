package com.github.kxbmap.configs


case class Bytes(value: Long) extends Ordered[Bytes] {
  def compare(that: Bytes): Int = value compare that.value

  def +(other: Bytes): Bytes = Bytes(value + other.value)
  def -(other: Bytes): Bytes = Bytes(value - other.value)
  def *(factor: Double): Bytes = Bytes((value * factor).toLong)
  def /(divisor: Double): Bytes = Bytes((value / divisor).toLong)
  def /(other: Bytes): Double = value / other.value.toDouble
  def unary_- : Bytes = Bytes(-value)
}


object Bytes {

  implicit val bytesAtPath: AtPath[Bytes] = AtPath {
    Bytes apply _.getBytes(_)
  }
  implicit val bytesListAtPath: AtPath[List[Bytes]] = AtPath {
    import scala.collection.JavaConversions._
    _.getBytesList(_).map(Bytes(_)).toList
  }

  implicit val bytesOrdering: Ordering[Bytes] = Ordering.by(_.value)

  final implicit class BytesMultiplication(val factor: Double) extends AnyVal {
    def *(bytes: Bytes): Bytes = bytes * factor
  }

}
