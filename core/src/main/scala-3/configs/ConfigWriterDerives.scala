package configs

trait ConfigWriterDerives:
  def derive[A](using ConfigKeyNaming[A]): ConfigWriter[A] = ???

  given [A](using ConfigKeyNaming[A]): ConfigWriter[A] = ???
