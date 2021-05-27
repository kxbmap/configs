package configs

trait ConfigReaderDerives:
  def derive[A](using ConfigKeyNaming[A]): ConfigReader[A] = ???
  def deriveBeanWith[A](newInstance: => A)(using ConfigKeyNaming[A]): ConfigReader[A] = ???

  given [A](using ConfigKeyNaming[A]): ConfigReader[A] = ???
