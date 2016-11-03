import sbt.Keys._
import sbt._

object BuildUtil {

  def scala211Only[A](xs: A*): Def.Initialize[Seq[A]] =
    Def.setting {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) => xs
        case _ => Seq.empty
      }
    }

}
