package configs

trait ConfigWriterDerives {

  def derive[A](implicit naming: ConfigKeyNaming[A]): ConfigWriter[A] =
    macro macros.ConfigWriterMacro.derive[A]


  implicit def autoDeriveConfigWriter[A](implicit naming: ConfigKeyNaming[A]): ConfigWriter[A] =
    macro macros.ConfigWriterMacro.derive[A]

}
