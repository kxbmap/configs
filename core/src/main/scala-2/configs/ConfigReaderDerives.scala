package configs

trait ConfigReaderDerives {

  def derive[A](implicit naming: ConfigKeyNaming[A]): ConfigReader[A] =
    macro macros.ConfigReaderMacro.derive[A]

  @deprecated("Use derive[A] or auto derivation", "0.5.0")
  def deriveBean[A](implicit naming: ConfigKeyNaming[A]): ConfigReader[A] =
    macro macros.ConfigReaderMacro.derive[A]

  def deriveBeanWith[A](newInstance: => A)(implicit naming: ConfigKeyNaming[A]): ConfigReader[A] =
    macro macros.ConfigReaderMacro.deriveBeanWith[A]


  implicit def autoDeriveConfigReader[A](implicit naming: ConfigKeyNaming[A]): ConfigReader[A] =
    macro macros.ConfigReaderMacro.derive[A]

}
