import sbt.Keys._
import sbt._

object BuildUtil {

  def byScalaVersion[A](f: PartialFunction[(Int, Int), Seq[A]]): Def.Initialize[Seq[A]] =
    Def.setting {
      CrossVersion.partialVersion(scalaVersion.value)
        .flatMap(f.lift)
        .getOrElse(Seq.empty)
    }

  def scala211Only[A](xs: A*): Def.Initialize[Seq[A]] =
    byScalaVersion {
      case (2, 11) => xs
    }

  def scala212Only[A](xs: A*): Def.Initialize[Seq[A]] =
    byScalaVersion {
      case (2, 12) => xs
    }

}
