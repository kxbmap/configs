package com.github.kxbmap.configs


case class Bytes(value: Long) extends Ordered[Bytes] {
  def compare(that: Bytes): Int = value compare that.value
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

}
